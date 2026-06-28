---
title: Ascendia
emoji: 🌍
colorFrom: blue
colorTo: indigo
sdk: docker
app_port: 7860
pinned: false
---

<div align="center">

# 🌍 Ascendia

**Sistema de Gestión Integral de Oportunidades Educativas Globales para Jóvenes**

*Prototipo operacional en Java — arquitectura por capas, patrón DAO, persistencia MySQL
y una vista web con globo terráqueo 3D.*

[![Java](https://img.shields.io/badge/Java-17%2B%20(web%2021)-007396?logo=openjdk&logoColor=white)](#requisitos)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1?logo=mysql&logoColor=white)](#capa-de-datos)
[![Javalin](https://img.shields.io/badge/Javalin-6.3.0-2E7D32)](#módulo-web--globo-3d-rf11)
[![globe.gl](https://img.shields.io/badge/globe.gl-2.34.4-1565C0)](#módulo-web--globo-3d-rf11)
[![Deploy](https://img.shields.io/badge/Deploy-HF%20Spaces%20(Docker)-FF9D00?logo=huggingface&logoColor=white)](#despliegue-en-producción)
[![Estado](https://img.shields.io/badge/Demo-en%20vivo-success)](https://jesusfar-ascendia.hf.space)

**🔗 Demo en vivo:** [jesusfar-ascendia.hf.space](https://jesusfar-ascendia.hf.space) ·
**📦 Repositorio:** [github.com/jesusfar/Ascendia](https://github.com/jesusfar/Ascendia)

</div>

---

## Tabla de contenidos

- [1. Visión general](#1-visión-general)
- [2. Características funcionales](#2-características-funcionales)
- [3. Arquitectura](#3-arquitectura)
- [4. Estructura del proyecto](#4-estructura-del-proyecto)
- [5. Stack tecnológico](#5-stack-tecnológico)
- [6. Requisitos](#6-requisitos)
- [7. Prototipo de consola — build y ejecución](#7-prototipo-de-consola--build-y-ejecución)
- [8. Capa de datos](#8-capa-de-datos)
- [9. Módulo web / Globo 3D (RF11)](#9-módulo-web--globo-3d-rf11)
- [10. Despliegue en producción](#10-despliegue-en-producción)
- [11. Reportes y evidencias](#11-reportes-y-evidencias)
- [12. Mapa de requisitos (trazabilidad)](#12-mapa-de-requisitos-trazabilidad)
- [13. Decisiones de diseño](#13-decisiones-de-diseño)
- [14. Documentación adicional](#14-documentación-adicional)
- [15. Créditos](#15-créditos)

---

## 1. Visión general

**Ascendia** es un prototipo operacional desarrollado en **Java** para gestionar,
consultar y recomendar oportunidades educativas internacionales —becas, cursos,
intercambios, pasantías y voluntariados—. Fue realizado para la materia **Seminario de
Práctica** de la Licenciatura en Informática de la **Universidad Empresarial Siglo 21**.

El proyecto se compone de **dos módulos complementarios** que comparten la misma capa de
datos:

| Módulo | Qué es | Tecnología |
|--------|--------|------------|
| **Núcleo de consola** (evaluable) | Aplicación de consola que demuestra POO, arquitectura por capas, patrón DAO, algoritmos de búsqueda/ordenamiento y persistencia opcional en MySQL. | Java 17+, JDBC |
| **Vista web / Globo 3D** (RF11, diferencial) | Otra vista sobre los **mismos DAO**: expone una API REST que alimenta un globo terráqueo 3D interactivo con las oportunidades geolocalizadas. | Java 21, Javalin/Jetty, globe.gl |

> 🎯 **Principio rector:** la vista web **no modifica** el prototipo de consola. Es un
> módulo aparte (`web/`) que **reutiliza** las clases de `src/` sin duplicarlas, de modo
> que el núcleo evaluable permanece intacto.

---

## 2. Características funcionales

El prototipo de consola ofrece **14 operaciones** desde un menú interactivo:

| # | Operación | Concepto demostrado |
|---|-----------|---------------------|
| 1 | Listar oportunidades activas | Recorrido de colecciones (`ArrayList`) |
| 2 | Buscar por filtros (tipo / continente) | Búsqueda multi-criterio |
| 3 | Buscar por palabra clave | Búsqueda lineal sobre texto |
| 4 | Ver detalle por ID | Acceso directo / encapsulamiento |
| 5 | Ordenar por fecha límite | **Ordenamiento por selección / inserción** |
| 6 | Buscar por ID | **Búsqueda binaria** sobre colección ordenada |
| 7 | Top 3 más próximas a vencer | Arreglo + `ArrayList` combinados |
| 8 | Registrar nueva oportunidad (admin) | Alta / validación |
| 9 | Modificar oportunidad (admin) | Actualización |
| 10 | Eliminar — borrado lógico (admin) | Cambio de estado (no DELETE físico) |
| 11 | Ver historial de consultas | **Pila (`Deque`)** — LIFO |
| 12 | Sugerencias para un usuario | Recomendación por áreas de interés |
| 13 | Reporte estadístico: por tipo, país y área | **Agregación / GROUP BY** |
| 14 | Exportar reporte de activas a CSV | **E/S de archivos** (`java.nio`, `BufferedWriter/Reader`) |

---

## 3. Arquitectura

### Arquitectura por capas (MVC simplificado + patrón DAO)

```text
┌──────────────────────────────────────────────────────────────────────┐
│  VISTA            MenuConsola (consola)   │   ServidorWeb + Globo 3D   │
│                   ── opción 1..14 ──      │   ── /api/* + globe.gl ──  │
├──────────────────────────────────────────┴────────────────────────────┤
│  SERVICIO         Buscador · Ordenador · HistorialConsultas · Exportador│
├────────────────────────────────────────────────────────────────────────┤
│  DAO (interfaces) IGenericoDAO · IOportunidadDAO · IUsuarioDAO          │
│        │                                                                │
│        ├── jdbc/      OportunidadDAOJDBC · UsuarioDAOJDBC  ──► MySQL 8  │
│        └── memoria/   OportunidadDAOMemoria · UsuarioDAOMemoria ─► RAM  │
├────────────────────────────────────────────────────────────────────────┤
│  MODELO           Oportunidad · Universidad · Pais · AreaEstudio ·      │
│                   Usuario · Favorito · EntidadBase + enums              │
├────────────────────────────────────────────────────────────────────────┤
│  UTIL             FabricaDAO (Factory) · ConexionDB · BaseMemoria (Singleton) │
└────────────────────────────────────────────────────────────────────────┘
```

### Patrones aplicados

- **DAO (Data Access Object):** la lógica de negocio depende solo de las **interfaces**
  `IOportunidadDAO` / `IUsuarioDAO`, nunca de la implementación concreta.
- **Factory (`FabricaDAO`):** decide en tiempo de ejecución si entrega la implementación
  **JDBC** (MySQL) o **memoria** (RAM), devolviendo siempre el tipo interfaz. Es el
  **corazón del polimorfismo** del sistema.
- **Singleton (`BaseMemoria`):** una única instancia mantiene las colecciones en memoria,
  sembradas con datos de prueba equivalentes a los de MySQL.

### Polimorfismo en acción

El mismo código de servicio funciona contra cualquier backend porque programa contra la
abstracción:

```java
FabricaDAO.setModo(FabricaDAO.Modo.JDBC);          // o .MEMORIA
IOportunidadDAO dao = FabricaDAO.getOportunidadDAO();
List<Oportunidad> activas = dao.listarActivas();   // no sabe si vino de MySQL o RAM
```

---

## 4. Estructura del proyecto

```text
ascendia/
├── src/com/ascendia/            # NÚCLEO de consola (28 tipos: 21 clases, 3 interfaces, 4 enums)
│   ├── Main.java                #   punto de entrada (elige backend; fallback a memoria)
│   ├── modelo/                  #   entidades del dominio + enumeraciones
│   ├── dao/                     #   interfaces DAO + DAOException
│   │   ├── jdbc/                #     persistencia real contra MySQL
│   │   └── memoria/             #     persistencia en RAM (ejecución sin servidor)
│   ├── servicio/                #   Buscador, Ordenador, HistorialConsultas, ExportadorReportes
│   ├── util/                    #   FabricaDAO, ConexionDB, BaseMemoria
│   └── vista/                   #   MenuConsola
│
├── web/                         # MÓDULO WEB (RF11) — reutiliza ../src, NO lo modifica
│   ├── pom.xml                  #   módulo Maven; build-helper + maven-shade (fat jar)
│   ├── src/main/java/com/ascendia/web/
│   │   └── ServidorWeb.java     #   API REST (Javalin/Jetty) + servidor de estáticos
│   ├── src/main/resources/public/
│   │   ├── index.html           #   globo 3D (globe.gl)
│   │   ├── app.js               #   carga de datos (fetch /api/oportunidades + fallback)
│   │   └── assets/              #   logos, texturas
│   └── scripts/ascendia-stack.ps1   # arranque/parada del stack para la demo (Windows)
│
├── database/
│   ├── ascendia_schema.sql      # esquema operativo + seed usado por la app
│   ├── ascendia_database.sql    # versión documentada del modelo (TP2)
│   └── README.md                # guía de importación y modelo SQL
│
├── deploy/
│   ├── Dockerfile               # (en la raíz) imagen multi-stage HF Spaces
│   ├── entrypoint.sh            # arranca MySQL 8, siembra y lanza el JAR
│   └── README.md                # documentación completa del despliegue
│
├── evidencias/                  # capturas + JSON de la API
├── Dockerfile                   # imagen de despliegue (HF Spaces / Docker)
├── INFORME_PARA_WORD.md         # informe técnico verificado (para el documento académico)
└── README.md                    # este archivo
```

---

## 5. Stack tecnológico

| Capa | Tecnología | Versión |
|------|-----------|---------|
| Lenguaje (núcleo) | Java | 17+ |
| Lenguaje (web) | Java | **21** (`maven.compiler.release=21`) |
| Base de datos | MySQL | 8.x |
| Driver JDBC | MySQL Connector/J | 9.1.0 |
| Framework web | Javalin (sobre **Jetty embebido**) | 6.3.0 |
| Serialización JSON | Jackson Databind | 2.17.2 |
| Logging | SLF4J Simple | 2.0.16 |
| Empaquetado | Maven Shade Plugin (fat jar) | 3.6.0 |
| Reuso de fuentes | Build Helper Maven Plugin | 3.6.0 |
| Globo 3D (front) | globe.gl | 2.34.4 |
| Motor 3D (front) | Three.js | r0.171.0 |
| Contenedor de deploy | Docker (HF Spaces, SDK Docker) | — |

**Estructuras de datos:** `ArrayList`, arreglos, `Deque` (como pila).
**Algoritmos:** búsqueda lineal, búsqueda binaria, ordenamiento por selección, ordenamiento por inserción.
**Manejo de errores:** excepciones controladas vía `DAOException` (RNF10).

---

## 6. Requisitos

**Modo memoria (núcleo):**
- JDK 17 o superior.

**Modo JDBC (núcleo con persistencia real):**
- JDK 17 o superior · MySQL Server 8.x · MySQL Connector/J en el classpath.

**Módulo web / globo:**
- JDK **21** · Maven 3.9+ · base `ascendia` cargada (o el contenedor Docker, que la trae).

---

## 7. Prototipo de consola — build y ejecución

### Compilación

**Windows (PowerShell):**
```powershell
Get-ChildItem -Recurse -Filter *.java src | ForEach-Object {
  $_.FullName.Substring((Get-Location).Path.Length + 1)
} | Set-Content -Encoding ASCII sources.txt
javac -encoding UTF-8 -d bin "@sources.txt"
```

**Linux / macOS / Git Bash:**
```bash
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
```

### Ejecución

El `Main` permite elegir el backend de forma **interactiva** o por **argumento**. Si se
elige JDBC y MySQL no está disponible, **cae automáticamente a memoria** sin interrumpir.

```bash
# Interactivo (pregunta MySQL o memoria; ENTER = memoria)
java -cp bin com.ascendia.Main

# Forzar memoria (sin MySQL)
java -cp bin com.ascendia.Main memoria

# Forzar JDBC (requiere el connector en el classpath)
#   Windows:
java -cp "bin;lib/mysql-connector-j.jar" com.ascendia.Main jdbc
#   Linux/macOS/Git Bash:
java -cp "bin:lib/mysql-connector-j.jar" com.ascendia.Main jdbc
```

---

## 8. Capa de datos

### Modos de persistencia

| Modo | Implementación | Cuándo usarlo |
|------|----------------|---------------|
| **Memoria** | `OportunidadDAOMemoria` / `UsuarioDAOMemoria` + `BaseMemoria` (Singleton) | Ejecución inmediata sin servidor; datos de prueba precargados. |
| **JDBC** | `OportunidadDAOJDBC` / `UsuarioDAOJDBC` + `ConexionDB` | Persistencia real contra MySQL 8. |

Ambos modos están **sembrados con el mismo dataset**, por lo que el comportamiento (y los
reportes) son idénticos.

### Configuración de conexión

`ConexionDB` lee la conexión por **variables de entorno**, con valores por defecto para un
entorno local (XAMPP). Esto permite que la web (RF11) y el contenedor Docker apunten a otra
base **sin tocar el código del prototipo**:

| Variable | Default local | Uso |
|----------|---------------|-----|
| `DB_URL` | `jdbc:mysql://localhost:3306/ascendia?useSSL=false&serverTimezone=UTC` | Cadena JDBC |
| `DB_USER` | `root` | Usuario |
| `DB_PASS` | *(vacío)* | Password |

### Esquema y datos (seed `database/ascendia_schema.sql`)

7 tablas (modelo 3FN del TP2): `pais`, `area_estudio`, `universidad`, `usuario`,
`oportunidad`, `favorito`, `preferencia_usuario`.

| Tabla | Filas del seed |
|-------|----------------|
| `pais` | 39 |
| `area_estudio` | 6 |
| `universidad` | 223 |
| `usuario` | 3 |
| `oportunidad` | 8 totales → **6 activas** (ids 1,2,3,5,6,7) |
| `preferencia_usuario` | 4 |

Las **6 oportunidades activas tienen `latitud`/`longitud` reales** — son los "faros" que
el globo 3D geolocaliza.

```bash
# Cargar el esquema y los datos
mysql -u root < database/ascendia_schema.sql
```

---

## 9. Módulo web / Globo 3D (RF11)

La vista web es **otra vista sobre la misma capa de datos**: arranca en modo JDBC, expone
los DAO como **API REST JSON** y sirve un **globo terráqueo 3D** (`globe.gl` / Three.js)
con las oportunidades geolocalizadas. El front y la API viven en el **mismo origen** (sin
CORS).

### Endpoints

| Método | Ruta | Devuelve |
|--------|------|----------|
| `GET` | `/api/health` | `ok` (health check) |
| `GET` | `/api/oportunidades` | JSON array de oportunidades **activas** (`listarActivas()`) |
| `GET` | `/api/oportunidades/{id}` | JSON de una oportunidad; `404` si no existe |
| `GET` | `/` , `/public/*` | Globo 3D estático (`index.html`, `app.js`, assets) |

### DTO de respuesta (`OportunidadDTO`)

```json
{
  "id": 7,
  "titulo": "Beca Fulbright para Doctorado en EE.UU.",
  "tipo": "BECA",
  "lat": 42.3601,
  "lng": -71.0942,
  "fechaApertura": null,
  "fechaLimite": "2026-06-30",
  "diasRestantes": 2,
  "modalidad": "PRESENCIAL",
  "idioma": "Ingles"
}
```

| Campo | Tipo | Nota |
|-------|------|------|
| `id` | `int` | |
| `titulo` | `String` | |
| `tipo` | `String` | enum en MAYÚSCULAS: `BECA`, `CURSO`, `PASANTIA`, … |
| `lat` / `lng` | `double` | coordenadas para el globo |
| `fechaApertura` | `String` | siempre `null` (el modelo no la persiste) |
| `fechaLimite` | `String` | ISO `yyyy-MM-dd` (o `null`) |
| `diasRestantes` | `long` | calculado |
| `modalidad` | `String` | `PRESENCIAL` / `VIRTUAL` / `HIBRIDA` |
| `idioma` | `String` | |

> El front (`app.js → cargarDatos()`) hace `fetch('/api/oportunidades')` y, si la API no
> responde, **cae a un dataset de respaldo embebido** (demo offline) para que el globo
> nunca quede vacío.

### Build y ejecución local

```bash
cd web
mvn -q clean package          # genera target/ascendia-web.jar (fat jar)

# Linux/macOS/Git Bash
DB_URL="jdbc:mysql://localhost:3306/ascendia?useSSL=false&serverTimezone=UTC" \
DB_USER=root DB_PASS= PORT=8080 \
java -jar target/ascendia-web.jar
# abrir http://localhost:8080
```

```powershell
# Windows PowerShell
cd web
mvn -q clean package
$env:DB_URL="jdbc:mysql://localhost:3306/ascendia?useSSL=false&serverTimezone=UTC"
$env:DB_USER="root"; $env:DB_PASS=""; $env:PORT="8080"
java -jar target/ascendia-web.jar
```

**Script de conveniencia** (Windows): `web/scripts/ascendia-stack.ps1 start|status|stop`
levanta/detiene MySQL + servidor web para la demo, sin afectar otros MySQL del sistema.

---

## 10. Despliegue en producción

La vista web está desplegada en **Hugging Face Spaces** con el **SDK Docker** (sin tarjeta
de crédito). Un **único contenedor** corre **JDK 21 + MySQL 8**: el fat jar sirve el globo
y la API en el puerto **7860**, y MySQL se **siembra al iniciar** desde el esquema.

> **🌐 En vivo:** [https://jesusfar-ascendia.hf.space](https://jesusfar-ascendia.hf.space)
> · health: [`/api/health`](https://jesusfar-ascendia.hf.space/api/health) →
> `ok` · datos: [`/api/oportunidades`](https://jesusfar-ascendia.hf.space/api/oportunidades)

### Prueba local con Docker

```bash
docker build -t ascendia .
docker run --rm -p 7860:7860 ascendia
# http://localhost:7860
```

> ℹ️ El disco del Space es **efímero**: la base se **reseed-ea en cada arranque** y la web
> es de **solo lectura**. La documentación completa del deploy —arquitectura, variables de
> entorno, creación del Space y la variante futura con **Aiven** para base persistente— está
> en **[`deploy/README.md`](deploy/README.md)**.

---

## 11. Reportes y evidencias

### RF13 — Reporte estadístico (opción 13)

Genera tres agregaciones de las oportunidades activas (idénticas en memoria y JDBC):

```text
POR TIPO        Beca:3   Curso:1   Intercambio:0   Pasantia:1   Voluntariado:1
POR PAIS        Estados Unidos:2   Alemania:1   Colombia:1   Francia:1   Japon:1
POR AREA        Ciencias Exactas:2   Ingenieria y Tecnologia:2   Ciencias Sociales:1   Educacion:1
```

### RF14 — Exportación a CSV (opción 14)

`ExportadorReportes` escribe las oportunidades activas a un CSV con `BufferedWriter` /
`java.nio.file.Files` (try-with-resources) y permite releerlo para verificar la persistencia.

### Evidencias incluidas (`evidencias/`)

| Archivo | Muestra |
|---------|---------|
| `01_compilacion.png` | Compilación exitosa |
| `02_alta_oportunidad.png` | Alta por consola |
| `03_sesion_jdbc.png` | Prototipo en modo JDBC |
| `04_mysql_verificacion.png` | Datos verificados en MySQL |
| `05_csv_contenido.png` | Contenido del CSV exportado |
| `api_oportunidades_globo.json` | Respuesta real de `/api/oportunidades` desde MySQL |

---

## 12. Mapa de requisitos (trazabilidad)

| Requisito | Dónde se implementa |
|-----------|---------------------|
| Listado / búsqueda / detalle | `MenuConsola` opciones 1–4 + `Buscador` |
| Ordenamiento (selección/inserción) | Opción 5 + `Ordenador` |
| Búsqueda binaria | Opción 6 + `Buscador` |
| ABM con borrado lógico | Opciones 8–10 + DAOs |
| Historial (pila) | Opción 11 + `HistorialConsultas` (`Deque`) |
| Sugerencias por área (RF14/CU-12) | Opción 12 + `sugerirPorAreas()` |
| **RF13** reporte por tipo/país/área | Opción 13 + `contarActivasPorPais/Area` |
| **RF14** exportación a archivo | Opción 14 + `ExportadorReportes` |
| **RF11** globo 3D geolocalizado | Módulo `web/` + `ServidorWeb` + `globe.gl` |
| Persistencia (memoria + JDBC) | `FabricaDAO`, DAOs `jdbc/` y `memoria/` |
| RNF10 manejo de errores | `DAOException` en toda la capa de datos |

---

## 13. Decisiones de diseño

- **Doble backend tras una interfaz.** El patrón DAO + Factory permite correr el prototipo
  sin MySQL (memoria) o con persistencia real (JDBC) cambiando una línea, demostrando
  polimorfismo de forma tangible.
- **La web no toca el núcleo.** El módulo `web/` agrega `../src` como *source root* vía
  `build-helper` y empaqueta todo en un fat jar; el código evaluable de consola queda
  intacto y versionado por separado.
- **Configuración por entorno.** `ConexionDB` lee `DB_URL/DB_USER/DB_PASS`, lo que permite
  el mismo binario en local, en la web y en Docker sin recompilar.
- **Javalin/Jetty como entrega, Tomcat/JSP como destino.** El prototipo web se implementó
  con Javalin 6.3.0 sobre Jetty embebido (Java 21, fat jar). La arquitectura Tomcat +
  Servlets + JSP se mantiene como **arquitectura destino**: ambas comparten el modelo de
  servlets de la especificación Jakarta, por lo que una migración futura preserva la capa
  de datos (DAO) sin cambios y solo reemplaza la capa de presentación/ruteo.
- **Deploy de un solo contenedor.** Para una demo sin costo ni tarjeta, MySQL viaja dentro
  del contenedor y se siembra al iniciar; la evolución natural (base persistente) es
  externalizar MySQL a Aiven (ver `deploy/README.md`).

---

## 14. Documentación adicional

| Documento | Contenido |
|-----------|-----------|
| [`deploy/README.md`](deploy/README.md) | Despliegue completo en HF Spaces: arquitectura, variables, build local, creación del Space, limitaciones y variante Aiven. |
| [`database/README.md`](database/README.md) | Guía de importación y descripción del modelo relacional (TP2). |
| [`INFORME_PARA_WORD.md`](INFORME_PARA_WORD.md) | Informe técnico verificado contra el código (conteo de clases, endpoints, RF13, datos, divergencias) para el documento académico. |

---

## 15. Créditos

**Alumno:** Jesús Fariña (VINF015570)
**Profesora:** Ana Carolina Ferreyra
**Materia:** Seminario de Práctica — Licenciatura en Informática
**Institución:** Universidad Empresarial Siglo 21

> Ascendia es un prototipo académico. Su foco está en demostrar una implementación clara de
> conceptos de programación, persistencia y organización por capas, sumando una vista web
> diferencial (globo 3D) sobre la misma base de datos.
