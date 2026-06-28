<#
.SYNOPSIS
    Arranca / detiene el stack del Globo 3D de Ascendia (RF11): MySQL portable + API web.

.DESCRIPTION
    Levanta el MySQL portable (si no esta corriendo) y el servidor web (fat jar)
    en modo JDBC, espera a que cada uno responda, y guarda los PID que el propio
    script inicio para poder detener SOLO esos procesos (no mata otros java/mysqld).

.PARAMETER Action
    start  : arranca MySQL (si hace falta) y el servidor web. (por defecto)
    stop   : detiene lo que este script haya arrancado.
    status : muestra el estado de MySQL, el servidor y la API.
    build  : compila el fat jar con Maven (mvn clean package).

.EXAMPLE
    .\ascendia-stack.ps1 start
    .\ascendia-stack.ps1 status
    .\ascendia-stack.ps1 stop

.NOTES
    Las rutas por defecto apuntan a la instalacion portable de este equipo.
    Se pueden sobreescribir por parametro, por ej.:
      .\ascendia-stack.ps1 start -MysqlHome 'D:\mysql' -Port 9090
#>
[CmdletBinding()]
param(
    [ValidateSet('start','stop','status','build')]
    [string]$Action = 'start',

    [string]$MysqlHome = 'C:\Users\sofia\tools\mysql-8.4.9-winx64',
    [string]$MysqlData = 'C:\Users\sofia\tools\mysql-data',
    [int]   $DbPort    = 3306,
    [string]$DbUser    = 'root',
    [string]$DbPass    = '',
    [string]$DbName    = 'ascendia',

    [int]   $Port      = 8080,
    [string]$JavaHome  = 'C:\Program Files\Java\jdk-25',
    [string]$MavenHome = 'C:\Users\sofia\tools\apache-maven-3.9.9'
)

$ErrorActionPreference = 'Stop'

# --- Rutas derivadas -------------------------------------------------------
$RepoWeb   = Resolve-Path (Join-Path $PSScriptRoot '..')   # ...\ascendia\web
$Jar       = Join-Path $RepoWeb 'target\ascendia-web.jar'
$Schema    = Join-Path $RepoWeb '..\database\ascendia_schema.sql'
$StateDir  = Join-Path $env:TEMP 'ascendia-stack'
$MysqlPid  = Join-Path $StateDir 'mysqld.pid'
$WebPid    = Join-Path $StateDir 'web.pid'
$MysqlLog  = Join-Path $StateDir 'mysqld.err.log'
$WebLog    = Join-Path $StateDir 'web.out.log'
$WebErr    = Join-Path $StateDir 'web.err.log'
# Nota: se delimitan las variables con ${...}. Sin las llaves, "$DbName?useSSL"
# es ambiguo para el parser de PowerShell y rompe el URL (la ? pegada al nombre).
$DbUrl     = "jdbc:mysql://localhost:${DbPort}/${DbName}?useSSL=false&serverTimezone=UTC"

New-Item -ItemType Directory -Force -Path $StateDir | Out-Null

# --- Helpers ---------------------------------------------------------------
function Test-Port([int]$p) {
    try { (Test-NetConnection -ComputerName 127.0.0.1 -Port $p -WarningAction SilentlyContinue).TcpTestSucceeded }
    catch { $false }
}

function Test-Http([string]$url) {
    try { (Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 2).StatusCode -eq 200 }
    catch { $false }
}

function Get-TrackedProcess([string]$pidFile) {
    if (-not (Test-Path $pidFile)) { return $null }
    $procId = Get-Content $pidFile -ErrorAction SilentlyContinue | Select-Object -First 1
    if (-not $procId) { return $null }
    try { Get-Process -Id ([int]$procId) -ErrorAction Stop } catch { $null }
}

function Wait-For([scriptblock]$cond, [int]$seconds, [string]$what) {
    foreach ($i in 1..$seconds) {
        if (& $cond) { Write-Host "  OK: $what (tras ${i}s)" -ForegroundColor Green; return $true }
        Start-Sleep -Seconds 1
    }
    Write-Host "  TIMEOUT esperando: $what" -ForegroundColor Red
    return $false
}

# --- Acciones --------------------------------------------------------------
function Start-MySQL {
    if (Test-Port $DbPort) {
        Write-Host "MySQL ya esta escuchando en $DbPort (no lo arranco yo)." -ForegroundColor Yellow
        return
    }
    $mysqld = Join-Path $MysqlHome 'bin\mysqld.exe'
    if (-not (Test-Path $mysqld)) { throw "No encuentro mysqld.exe en $mysqld" }
    Write-Host "Arrancando MySQL portable..." -ForegroundColor Cyan
    $p = Start-Process -FilePath $mysqld `
        -ArgumentList '--no-defaults', "--basedir=$MysqlHome", "--datadir=$MysqlData", "--port=$DbPort", '--console' `
        -PassThru -NoNewWindow -RedirectStandardError $MysqlLog
    $p.Id | Set-Content $MysqlPid
    if (-not (Wait-For { Test-Port $DbPort } 30 "MySQL en puerto $DbPort")) {
        Write-Host "  Ultimas lineas del log:" -ForegroundColor Red
        Get-Content $MysqlLog -Tail 15 -ErrorAction SilentlyContinue
        throw "MySQL no levanto."
    }
}

function Start-Web {
    if (-not (Test-Path $Jar)) {
        Write-Host "No existe el jar ($Jar). Compilando primero..." -ForegroundColor Yellow
        Invoke-Build
    }
    if (Test-Http "http://localhost:$Port/api/health") {
        Write-Host "El servidor web ya responde en $Port." -ForegroundColor Yellow
        return
    }
    Write-Host "Arrancando servidor web (globo) en modo JDBC..." -ForegroundColor Cyan
    $env:JAVA_HOME = $JavaHome
    $env:DB_URL = $DbUrl; $env:DB_USER = $DbUser; $env:DB_PASS = $DbPass; $env:PORT = "$Port"
    $java = Join-Path $JavaHome 'bin\java.exe'
    if (-not (Test-Path $java)) { $java = 'java' }   # cae al java del PATH
    $p = Start-Process -FilePath $java -ArgumentList '-jar', $Jar `
        -PassThru -NoNewWindow -RedirectStandardOutput $WebLog -RedirectStandardError $WebErr
    $p.Id | Set-Content $WebPid
    if (Wait-For { Test-Http "http://localhost:$Port/api/health" } 30 "API /api/health") {
        Write-Host ""
        Write-Host "  Globo:   http://localhost:$Port" -ForegroundColor Green
        Write-Host "  API:     http://localhost:$Port/api/oportunidades" -ForegroundColor Green
    } else {
        Write-Host "  Log del servidor:" -ForegroundColor Red
        Get-Content $WebErr -Tail 15 -ErrorAction SilentlyContinue
    }
}

function Stop-Tracked([string]$pidFile, [string]$name) {
    $proc = Get-TrackedProcess $pidFile
    if ($proc) {
        Write-Host "Deteniendo $name (PID $($proc.Id))..." -ForegroundColor Cyan
        Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
    } else {
        Write-Host "$name no estaba arrancado por este script." -ForegroundColor Yellow
    }
    Remove-Item $pidFile -ErrorAction SilentlyContinue
}

# Detiene cualquier mysqld ligado A NUESTRO data dir (no toca otros MySQL del sistema).
function Stop-OurMysql {
    $dataNorm = ([System.IO.Path]::GetFullPath($MysqlData)).TrimEnd('\')
    $procs = Get-CimInstance Win32_Process -Filter "Name='mysqld.exe'" -ErrorAction SilentlyContinue |
             Where-Object { $_.CommandLine -and ($_.CommandLine -replace '/','\') -like "*$dataNorm*" }
    foreach ($p in $procs) {
        Write-Host "Deteniendo mysqld ligado a nuestro data dir (PID $($p.ProcessId))..." -ForegroundColor Cyan
        Stop-Process -Id $p.ProcessId -Force -ErrorAction SilentlyContinue
    }
}

function Invoke-Build {
    $mvn = Join-Path $MavenHome 'bin\mvn.cmd'
    if (-not (Test-Path $mvn)) { $mvn = 'mvn' }
    $env:JAVA_HOME = $JavaHome
    Write-Host "Compilando fat jar con Maven..." -ForegroundColor Cyan
    Push-Location $RepoWeb
    try { & $mvn -B -ntp clean package } finally { Pop-Location }
}

function Show-Status {
    $mysqlUp = Test-Port $DbPort
    $apiUp   = Test-Http "http://localhost:$Port/api/health"
    Write-Host "== Estado del stack Ascendia ==" -ForegroundColor Cyan
    Write-Host ("  MySQL ({0}): {1}" -f $DbPort, $(if ($mysqlUp) {'ARRIBA'} else {'abajo'}))
    Write-Host ("  Web   ({0}): {1}" -f $Port,   $(if ($apiUp)   {'ARRIBA'} else {'abajo'}))
    if ($apiUp) {
        try {
            $n = (Invoke-WebRequest "http://localhost:$Port/api/oportunidades" -UseBasicParsing).Content
            $count = ($n | ConvertFrom-Json).Count
            Write-Host "  /api/oportunidades -> $count oportunidades (desde MySQL)" -ForegroundColor Green
        } catch {
            Write-Host "  /api/oportunidades -> error (MySQL caido? -> el globo usa el respaldo embebido)" -ForegroundColor Yellow
        }
    }
}

# --- Dispatch --------------------------------------------------------------
switch ($Action) {
    'start'  { Start-MySQL; Start-Web; Write-Host ""; Show-Status }
    'stop'   { Stop-Tracked $WebPid 'servidor web'; Stop-Tracked $MysqlPid 'MySQL'; Stop-OurMysql; Write-Host "Stack detenido." -ForegroundColor Green }
    'status' { Show-Status }
    'build'  { Invoke-Build }
}
