# Informe para alinear el documento Word con lo implementado

**Proyecto:** Ascendia — Sistema de Gestión de Oportunidades Educativas Globales
**Verificado contra el código real del repo el:** 2026-06-28
**Rama:** `main` (ya contiene todo — ver §6)

Todo lo de abajo está **verificado contra el código fuente y contra el deploy en
vivo**, no de memoria. Donde el Word dice algo distinto, lo marco como **CORREGIR**.

---

## 1. CONTEO DE CLASES  *(el Word dice "24 clases" → CORREGIR)*

Conteo exacto por archivo `.java` (un tipo de nivel superior por archivo):

### Núcleo (prototipo de consola) — `src/` = **28 archivos `.java`**

Desglose por tipo de Java:

| Categoría | Cant. | Archivos |
|-----------|-------|----------|
| **Clases** (incl. abstracta `EntidadBase` y excepción `DAOException`) | **21** | Main, DAOException, OportunidadDAOJDBC, UsuarioDAOJDBC, OportunidadDAOMemoria, UsuarioDAOMemoria, AreaEstudio, EntidadBase, Favorito, Oportunidad, Pais, Universidad, Usuario, Buscador, ExportadorReportes, HistorialConsultas, Ordenador, BaseMemoria, ConexionDB, FabricaDAO, MenuConsola |
| **Interfaces** | **3** | IGenericoDAO, IOportunidadDAO, IUsuarioDAO |
| **Enums** | **4** | EstadoOportunidad, Modalidad, RolUsuario, TipoOportunidad |
| **TOTAL tipos núcleo** | **28** | |

### Módulo web — `web/src/` = **1 archivo `.java`**

- `ServidorWeb.java` → **1 clase** + **1 `record` anidado** (`OportunidadDTO`).

### Totales (elegí la definición que use el Word y sé consistente)

| Definición | Núcleo | Web | **Total** |
|------------|--------|-----|-----------|
| Solo **clases** (sentido estricto) | 21 | 1 | **22** |
| **Todos los tipos** (clases + interfaces + enums + record) | 28 | 2 | **30** |
| **Archivos `.java`** | 28 | 1 | **29** |

**Sugerencia de redacción para el Word:** *"El sistema se compone de 28 tipos en el
núcleo de consola (21 clases, 3 interfaces y 4 enumeraciones) más el módulo web
(`ServidorWeb` y su DTO), totalizando 29 archivos `.java`."* Reemplaza el "24
clases" actual, que ya no es exacto.

---

## 2. ARQUITECTURA WEB REAL

### Tecnología y versiones (verificadas en `web/pom.xml`)

| Componente | Versión exacta |
|------------|----------------|
| **Javalin** | **6.3.0** (`io.javalin:javalin`) |
| **Servlet container** | **Jetty embebido** (dependencia transitiva de Javalin 6.3.0) |
| **Java / compiler release** | **21** (`maven.compiler.release = 21`) |
| **Jackson** (serialización JSON) | 2.17.2 |
| **SLF4J** (logging) | slf4j-simple 2.0.16 |
| **MySQL Connector/J** | 9.1.0 |
| **Empaquetado** | **fat jar** ejecutable vía `maven-shade-plugin` 3.6.0 (`target/ascendia-web.jar`, main-class `com.ascendia.web.ServidorWeb`) |
| **Reuso del núcleo** | `build-helper-maven-plugin` 3.6.0 agrega `../src` como *source root* → el JAR incluye los DAO/JDBC del prototipo **sin duplicarlos** |

La vista web arranca **siempre en modo JDBC** (`FabricaDAO.setModo(JDBC)`): es otra
vista sobre la misma capa de datos (MySQL), no modifica el prototipo de consola.

### Endpoints expuestos (verificados en `ServidorWeb.java`)

| Método | Ruta | Devuelve |
|--------|------|----------|
| `GET` | `/api/health` | `ok` (texto plano) — health check |
| `GET` | `/api/oportunidades` | **JSON array** de oportunidades **activas** (`dao.listarActivas()`) — los "faros" del globo |
| `GET` | `/api/oportunidades/{id}` | **JSON** de una oportunidad (`buscarPorId`); `404 "No encontrada"` si no existe |
| *(estáticos)* | `/` y `/public/*` | Globo 3D (`index.html`, `app.js`, assets) servidos desde el classpath |

### DTO de oportunidad — `record OportunidadDTO` (campos y tipos exactos)

```java
record OportunidadDTO(
    int    id,
    String titulo,
    String tipo,           // enum .name() en MAYÚSCULAS: BECA, CURSO, PASANTIA, ...
    double lat,            // latitud
    double lng,            // longitud
    String fechaApertura,  // siempre null (el modelo no la persiste; el front lo tolera)
    String fechaLimite,    // ISO yyyy-MM-dd (o null)
    long   diasRestantes,
    String modalidad,      // PRESENCIAL / VIRTUAL / HIBRIDA (o null)
    String idioma
)
```

### Frase técnica exacta para reconciliar con Tomcat/Servlets/JSP en el Word

> *"El prototipo entregado implementa la vista web con **Javalin 6.3.0 sobre un
> contenedor de servlets Jetty embebido** (Java 21, empaquetado como fat jar
> ejecutable), reutilizando los DAO/JDBC del núcleo. La arquitectura **Tomcat +
> Servlets + JSP** descrita en el diseño se mantiene como **arquitectura destino**;
> ambas comparten el mismo modelo de servlets de la especificación Jakarta, por lo
> que la migración futura preserva la capa de datos (patrón DAO) sin cambios y solo
> reemplaza la capa de presentación/ruteo."*

---

## 3. RF13 — Reporte estadístico por tipo, país y área

### Métodos que lo implementan (nombres reales)

| Dimensión | Dónde está | Método / mecanismo |
|-----------|-----------|--------------------|
| **Por tipo** | `vista/MenuConsola.java` → `reportePorTipo()` | Recorre `listarActivas()` y cuenta en un **arreglo indexado por `TipoOportunidad.ordinal()`** (muestra **todos** los tipos del enum, incluso los de conteo 0). |
| **Por país** | `IOportunidadDAO.contarActivasPorPais()` | **JDBC:** `OportunidadDAOJDBC.contarActivasPorPais()` (GROUP BY en SQL con JOIN universidad→país). **Memoria:** `OportunidadDAOMemoria.contarActivasPorPais()` (agrupa en Java sobre los objetos). |
| **Por área** | `IOportunidadDAO.contarActivasPorArea()` | **JDBC:** `OportunidadDAOJDBC.contarActivasPorArea()` (GROUP BY con JOIN a `area_estudio`). **Memoria:** `OportunidadDAOMemoria.contarActivasPorArea()`. |

Orquestados desde `MenuConsola.reporteEstadistico()` (opción 13 del menú, CU-11).
Ambas dimensiones país/área se **ordenan por cantidad desc y luego por nombre asc**
(en JDBC vía `ORDER BY`, en memoria vía `ordenarPorCantidadDesc`).

### Salida real verificada (idéntica en **ambos modos**)

> Los datos sembrados en memoria (`BaseMemoria`) y en MySQL (`ascendia_schema.sql`)
> son **los mismos 6 registros activos** (ids 1, 2, 3, 5, 6, 7; el id 8 está
> *vencida*). Por eso la salida de los tres reportes es **idéntica en modo memoria y
> en modo JDBC**. El conteo "por tipo" fue además **confirmado contra la API en vivo**
> (`/api/oportunidades` del Space): 3 BECA, 1 CURSO, 1 PASANTIA, 1 VOLUNTARIADO.

**REPORTE — OPORTUNIDADES ACTIVAS POR TIPO** *(muestra todos los tipos del enum)*
```
  Beca          : 3
  Curso         : 1
  Intercambio   : 0
  Pasantia      : 1
  Voluntariado  : 1
```

**REPORTE — OPORTUNIDADES ACTIVAS POR PAIS** *(orden: cantidad desc, nombre asc)*
```
  Estados Unidos : 2
  Alemania       : 1
  Colombia       : 1
  Francia        : 1
  Japon          : 1
```

**REPORTE — OPORTUNIDADES ACTIVAS POR AREA DE ESTUDIO** *(orden: cantidad desc, nombre asc)*
```
  Ciencias Exactas        : 2
  Ingenieria y Tecnologia : 2
  Ciencias Sociales       : 1
  Educacion               : 1
```

> Total = 6 oportunidades activas en las tres dimensiones (consistencia verificada).

---

## 4. BASE DE DATOS Y DATOS

| Aspecto | Valor verificado |
|---------|------------------|
| **Motor local** | **MySQL 8.x** (driver MySQL Connector/J 9.1.0). |
| **Motor en el deploy** | **MySQL 8.0** dentro del contenedor (paquete `mysql-server` de Ubuntu Jammy). **NO** es MariaDB — el deploy corre MySQL 8 real (el ajuste `--no-defaults` fue para correrlo como uid 1000, no un fallback de motor). |
| **Script de seed usado** | `database/ascendia_schema.sql` (CREATE TABLE IF NOT EXISTS + INSERTs). |
| **Tablas creadas** | `pais`, `area_estudio`, `universidad`, `usuario`, `oportunidad`, `favorito`, `preferencia_usuario` (7 tablas). |

### Nº de filas del seed (`ascendia_schema.sql`)

| Tabla | Filas |
|-------|-------|
| `pais` | **39** |
| `area_estudio` | 6 |
| `universidad` | **223** |
| `usuario` | 3 |
| `oportunidad` | **8 totales → 6 activas** (ids 1,2,3,5,6,7; id 8 *vencida*; no hay id 4) |
| `preferencia_usuario` | 4 |
| `favorito` | 0 |

### Coordenadas reales

**Sí**, las **6 oportunidades activas tienen `latitud`/`longitud` reales** (no
ficticias) cargadas en la propia tabla `oportunidad` — son las que el globo usa como
faros. Ejemplos: Beca DAAD (Berlín, 52.51253 / 13.32693), Erasmus Mundus (París,
48.84794 / 2.35639), Curso IA MIT y Fulbright (Cambridge MA, 42.3601 / -71.0942),
Pasantía Tokyo (35.7126 / 139.762), Voluntariado Andes (Bogotá, 4.60127 / -74.06492).
Además `pais` y `universidad` traen sus propias coordenadas reales.

---

## 5. GLOBO / RF11

| Aspecto | Valor verificado |
|---------|------------------|
| **Librería del globo** | **globe.gl 2.34.4** (`https://unpkg.com/globe.gl@2.34.4`). |
| **Motor 3D** | **Three.js r0.171.0** (módulo ES, misma revisión que globe.gl; usado para el modelo "Marvin"). |
| **Consumo de la API** | `app.js → cargarDatos()` hace `fetch('/api/oportunidades')`. Si la respuesta no es `ok`, cae a un **array de datos de respaldo embebido** (demo offline) → el globo nunca queda vacío. |
| **Capas geográficas** | países/estados/ciudades desde GeoJSON de `natural-earth-vector` (con `.catch()` que degrada a esfera lisa si no hay red). |

### Evidencias disponibles

- **`evidencias/api_oportunidades_globo.json`** — respuesta JSON real de
  `/api/oportunidades` servida desde MySQL (las 6 activas con lat/lng, tipo,
  modalidad, idioma, díasRestantes). **Ya existe como archivo.**
- **URL pública del Space (en vivo, verificada hoy):**
  **https://jesusfar-ascendia.hf.space** → globo + API.
  `https://jesusfar-ascendia.hf.space/api/oportunidades` devuelve las 6 activas.

---

## 6. ENLACE GITHUB Y ESTADO

- **Repositorio:** **https://github.com/jesusfar/Ascendia**
- **Estado de `main` (post-Tarea 1):** `main` remoto ya **contiene todo** el trabajo
  (TP4 + `web/` + globo + `Dockerfile` + `deploy/` + `database/` + `src/` con
  `ExportadorReportes` + README + `evidencias/`). Se sobrescribió el `main` viejo
  (que era un *Revert*) con `--force-with-lease`.
- **HEAD de `origin/main`:** `16081f3` (commit *"Globo: etiquetas sin tildes ... 10
  consejos al azar"*).
- **Deploy:** Space en HF **Running** → https://jesusfar-ascendia.hf.space

---

## 7. LISTA DE FIGURAS / CAPTURAS para el anexo

### Ya existen como archivo (en `evidencias/`)

| Archivo | Qué muestra |
|---------|-------------|
| `evidencias/01_compilacion.png` | Compilación exitosa del prototipo. |
| `evidencias/02_alta_oportunidad.png` | Alta de una oportunidad por consola. |
| `evidencias/03_sesion_jdbc.png` | Sesión del prototipo corriendo en modo JDBC. |
| `evidencias/04_mysql_verificacion.png` | Verificación de datos en MySQL (Workbench / cliente). |
| `evidencias/05_csv_contenido.png` | Contenido del CSV exportado (RF14 / ExportadorReportes). |
| `evidencias/api_oportunidades_globo.json` | Respuesta JSON de `/api/oportunidades` desde MySQL. |

### Faltan — las tenés que sacar vos manualmente

| Captura sugerida | Qué debería mostrar | Cómo obtenerla |
|------------------|--------------------|-----------------|
| **Globo 3D en el navegador** | El globo con los 6 faros y un panel de detalle abierto. | Abrir https://jesusfar-ascendia.hf.space y capturar pantalla. |
| **Reporte RF13 en consola** | La salida de la opción 13 (por tipo / país / área). | Ejecutar el prototipo (`java -cp bin com.ascendia.Main`), opción 13, capturar. |
| **DevTools → Network** | El request `GET /api/oportunidades` con status 200 y el JSON de respuesta. | En el navegador, F12 → Network → recargar el globo → click en `oportunidades`. |
| *(opcional)* **Health check** | `GET /api/health → ok` en el navegador. | Abrir https://jesusfar-ascendia.hf.space/api/health. |

---

## 8. DIVERGENCIAS detectadas Word ↔ implementación (y cómo cerrarlas)

| # | Lo que probablemente dice el Word | Realidad implementada | Cómo cerrarla |
|---|-----------------------------------|-----------------------|----------------|
| 1 | "24 clases" | 28 tipos en el núcleo (21 clases + 3 interfaces + 4 enums) + módulo web | **Ajustar texto** (ver §1: usar "28 tipos / 29 archivos"). |
| 2 | Arquitectura web = Tomcat + Servlets + JSP | Javalin 6.3.0 + Jetty embebido + fat jar (Java 21) | **Ajustar texto:** declarar Javalin/Jetty como lo **entregado** y Tomcat/JSP como **arquitectura destino** (frase exacta en §2). No tocar código. |
| 3 | "Java 17 o superior" (README y posiblemente el Word) | El **núcleo** compila con JDK 17+, pero el **módulo web** exige **Java 21** (`maven.compiler.release=21`) y el deploy usa JDK 21. | **Ajustar texto:** aclarar "núcleo Java 17+, módulo web y deploy Java 21". |
| 4 | RF13 solo "por tipo" (README lista solo "reporte por tipo") | Implementa **tipo + país + área** | **Ajustar texto:** mencionar las tres dimensiones (ya están en el menú, opción 13). |
| 5 | Evidencias = "evidencias_ascendia_capturas.zip" (README) | En el repo están las PNG sueltas en `evidencias/` + el JSON; no hay zip versionado. | **Ajustar texto** del README/Word para apuntar a `evidencias/` y a la URL del Space, o **agregar** el zip si se quiere conservar la referencia. |
| 6 | Posible mención a base persistente / datos que se guardan | El deploy tiene **disco efímero**: la base se **reseed-ea en cada arranque** y las altas no persisten. | **Ajustar texto:** aclarar que el Space es demo de **solo lectura**; mencionar la variante Aiven (ver `deploy/README.md`) como evolución para persistencia. |
| 7 | Conteo de oportunidades / universidades genérico | 6 activas (de 8), **223** universidades, **39** países en el seed. | **Ajustar texto** con las cifras exactas de §4. |

> En todos los casos la divergencia se cierra **ajustando el texto del Word**, no
> tocando código: lo implementado es coherente y está verificado. La única decisión
> de fondo es de redacción: presentar Javalin/Jetty como entrega y Tomcat/JSP como
> destino (recomendado), en lugar de afirmar que ya se usa Tomcat.
