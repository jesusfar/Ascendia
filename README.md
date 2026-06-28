# Ascendia

**Sistema de Gestion Integral de Oportunidades Educativas Globales para Jovenes**

Ascendia es un prototipo operacional desarrollado en **Java** para gestionar,
consultar y recomendar oportunidades educativas internacionales, como becas,
cursos, intercambios, pasantias y voluntariados.

El proyecto fue realizado para la materia **Seminario de Practica** de la
Licenciatura en Informatica de la **Universidad Empresarial Siglo 21**.

**Alumno:** Jesus Farina (VINF015570)  
**Profesora:** Ana Carolina Ferreyra

## Objetivo

El objetivo del prototipo es demostrar una solucion de consola funcional que
integra principios de Programacion Orientada a Objetos, arquitectura por capas,
patron DAO, persistencia opcional en MySQL y algoritmos de busqueda y
ordenamiento aplicados a un dominio realista.

Ascendia permite:

- Listar oportunidades educativas activas.
- Buscar oportunidades por tipo, continente o palabra clave.
- Consultar el detalle de una oportunidad por ID.
- Ordenar oportunidades por fecha limite.
- Buscar oportunidades mediante busqueda binaria.
- Registrar nuevas oportunidades.
- Consultar historial de oportunidades revisadas.
- Obtener sugerencias segun las preferencias academicas de un usuario.
- Generar un reporte de oportunidades activas por tipo.

## Caracteristicas Tecnicas

- **Lenguaje:** Java 17 o superior.
- **Interfaz:** aplicacion de consola.
- **Arquitectura:** MVC simplificado con separacion por paquetes.
- **Persistencia:** modo memoria por defecto y modo JDBC con MySQL.
- **Base de datos:** MySQL 8.x.
- **Patrones aplicados:** DAO, Factory y Singleton.
- **Estructuras:** `ArrayList`, arreglos y `Deque` como pila.
- **Algoritmos:** busqueda lineal, busqueda binaria, ordenamiento por seleccion
  y ordenamiento por insercion.
- **Manejo de errores:** excepciones controladas mediante `DAOException`.

## Arquitectura del Proyecto

```text
src/com/ascendia/
  Main.java
  modelo/       Entidades del dominio y enumeraciones
  dao/          Interfaces DAO, excepcion e implementaciones
  dao/jdbc/     Persistencia JDBC contra MySQL
  dao/memoria/  Persistencia en memoria para ejecucion rapida
  servicio/     Busqueda, ordenamiento e historial
  util/         ConexionDB, BaseMemoria y FabricaDAO
  vista/        MenuConsola

database/
  ascendia_schema.sql     Script operativo usado por el prototipo Java
  ascendia_database.sql   Script documentado de base de datos para TP2
  README.md               Guia de importacion y descripcion del modelo SQL
```

## Requisitos

Para ejecutar el modo en memoria:

- JDK 17 o superior.

Para ejecutar el modo JDBC:

- JDK 17 o superior.
- MySQL Server 8.x.
- MySQL Connector/J en el classpath.

## Compilacion

En Windows PowerShell:

```powershell
Get-ChildItem -Recurse -Filter *.java src | ForEach-Object {
  $_.FullName.Substring((Get-Location).Path.Length + 1)
} | Set-Content -Encoding ASCII sources.txt

javac -encoding UTF-8 -d bin "@sources.txt"
```

En Linux, macOS o Git Bash:

```bash
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
```

## Ejecucion en Modo Memoria

El modo memoria no requiere MySQL. Es la forma mas simple de ejecutar el
prototipo.

```bash
java -cp bin com.ascendia.Main
```

La aplicacion inicia con datos de prueba precargados desde `BaseMemoria`.

## Ejecucion con MySQL

1. Crear la base de datos y cargar datos iniciales:

```bash
mysql -u root < database/ascendia_schema.sql
```

2. Descargar MySQL Connector/J desde Maven Central o el sitio oficial de MySQL.

3. Colocar el archivo `.jar` dentro de una carpeta `lib/`, por ejemplo:

```text
lib/mysql-connector-j.jar
```

4. Ejecutar la aplicacion en modo JDBC:

En Windows:

```powershell
java -cp "bin;lib/mysql-connector-j.jar" com.ascendia.Main jdbc
```

En Linux, macOS o Git Bash:

```bash
java -cp "bin:lib/mysql-connector-j.jar" com.ascendia.Main jdbc
```

La configuracion por defecto de conexion se encuentra en:

```text
src/com/ascendia/util/ConexionDB.java
```

Valores predeterminados:

```text
URL:  jdbc:mysql://localhost:3306/ascendia?useSSL=false&serverTimezone=UTC
USER: root
PASS: vacio
```

## Base de Datos

El repositorio incluye dos archivos SQL:

- `database/ascendia_schema.sql`: esquema y datos utilizados por la aplicacion
  Java en modo JDBC.
- `database/ascendia_database.sql`: version documentada del modelo relacional,
  con comentarios, consultas y operaciones SQL asociadas al TP2.

Tablas principales:

- `pais`
- `universidad`
- `usuario`
- `area_estudio`
- `oportunidad`
- `favorito`
- `preferencia_usuario`

## Capa Web / Globo 3D (RF11 - caracteristica diferencial)

Ademas del prototipo de consola (nucleo evaluable), el repositorio incluye el
modulo `web/`: una **vista web** que reutiliza los DAO existentes y expone una
**API REST** que alimenta un **globo terraqueo 3D** (`globe.gl` / Three.js) con
las oportunidades educativas. El prototipo de consola **no se modifica**; la web
es un modulo aparte que comparte la misma capa de datos (MySQL via JDBC).

```text
web/
  pom.xml                          Modulo Maven; reutiliza ../src y genera un fat jar
  src/main/java/com/ascendia/web/
    ServidorWeb.java               API REST (Javalin/Jetty) + servidor de estaticos
  src/main/resources/public/       Frontend del globo (index.html, app.js, assets...)
```

**Endpoints:**

- `GET /api/health` &rarr; `ok`
- `GET /api/oportunidades` &rarr; oportunidades activas (los "faros" del globo), desde MySQL.
- `GET /api/oportunidades/{id}` &rarr; detalle de una oportunidad.

El DTO servido coincide con lo que consume `app.js`:
`{id, titulo, tipo, lat, lng, fechaApertura, fechaLimite, diasRestantes, modalidad, idioma}`.
Si la API no esta disponible, el globo cae a un set de datos de respaldo embebido
(demo offline), sin dejar de funcionar.

**Build y ejecucion** (requiere Maven y la base `ascendia` cargada):

```bash
cd web
mvn -q clean package
DB_URL="jdbc:mysql://localhost:3306/ascendia?useSSL=false&serverTimezone=UTC" \
DB_USER=root DB_PASS= PORT=8080 \
java -jar target/ascendia-web.jar
# abrir http://localhost:8080
```

En Windows PowerShell:

```powershell
cd web
mvn -q clean package
$env:DB_URL="jdbc:mysql://localhost:3306/ascendia?useSSL=false&serverTimezone=UTC"
$env:DB_USER="root"; $env:DB_PASS=""; $env:PORT="8080"
java -jar target/ascendia-web.jar
```

La conexion se configura por variables de entorno (`DB_URL`, `DB_USER`, `DB_PASS`,
`PORT`); si no se definen, `ConexionDB` mantiene los valores por defecto del
entorno local (sin afectar a la consola).

**Script de conveniencia** (Windows PowerShell): `web/scripts/ascendia-stack.ps1`
levanta y detiene el stack (MySQL + servidor web) para la demo:

```powershell
.\web\scripts\ascendia-stack.ps1 start    # arranca MySQL + globo
.\web\scripts\ascendia-stack.ps1 status   # estado y nro de oportunidades servidas
.\web\scripts\ascendia-stack.ps1 stop     # detiene ambos
```

Las rutas (MySQL, JDK, Maven) son parametros con valores por defecto y se pueden
sobreescribir, por ej. `-MysqlHome`, `-Port`. Solo detiene el `mysqld` ligado a su
propio data dir, sin tocar otros MySQL del sistema.

## Evidencias

El archivo `evidencias_ascendia_capturas.zip` contiene capturas de pantalla del
proyecto compilando y funcionando, incluyendo:

- Estructura del proyecto.
- Compilacion exitosa.
- Menu principal.
- Listado y busquedas.
- Detalle e historial.
- Algoritmos y reportes.
- Ejecucion con MySQL mediante JDBC.

## Alcance

Ascendia es un prototipo academico de consola. Su foco esta en demostrar una
implementacion clara de conceptos de programacion, persistencia y organizacion
por capas, no en proveer una interfaz grafica o un sistema productivo completo.

## Autor

**Jesus Farina**  
Licenciatura en Informatica  
Universidad Empresarial Siglo 21
