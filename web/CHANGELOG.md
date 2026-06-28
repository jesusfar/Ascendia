# CHANGELOG — Capa web / Globo 3D (RF11)

Caracteristica diferencial agregada sobre el prototipo de consola, sin modificar
su comportamiento. La web es un modulo aparte que **reutiliza** los DAO del backend.

## Agregado

- **Modulo Maven `web/`** (`web/pom.xml`): reutiliza `../src` via
  `build-helper-maven-plugin` (no duplica codigo) y genera un fat jar ejecutable
  `target/ascendia-web.jar` con `maven-shade-plugin`.
  Dependencias: Javalin 6.3.0, jackson-databind (requerido por `ctx.json()`),
  slf4j-simple, mysql-connector-j.
- **`ServidorWeb.java`** (`com.ascendia.web`): servidor Javalin (sobre Jetty,
  contenedor de servlets embebido) en modo JDBC. Sirve los estaticos del globo y
  expone la API REST:
  - `GET /api/health`
  - `GET /api/oportunidades` — oportunidades activas (faros del globo), desde MySQL.
  - `GET /api/oportunidades/{id}` — detalle por id.
  - DTO: `{id, titulo, tipo, lat, lng, fechaApertura, fechaLimite, diasRestantes, modalidad, idioma}`.
- **Frontend del globo** copiado a `web/src/main/resources/public/`
  (`index.html`, `app.js`, `marvin.js`, `universidades.js`, `assets/`). Se
  excluyeron archivos de desarrollo (`tools/`, `uploads/`, `screenshots/`, etc.).

## Nombres de lugares en español (globo)

- **`public/app.js`**: las etiquetas geográficas (Natural Earth) venían en inglés.
  Ahora se muestran en español:
  - **Países** → campo `NAME_ES` del geojson (corrige "Falkland Is." → **"Islas
    Malvinas"** y agrega acentos: España, Japón, Perú…).
  - **Provincias/estados** → campo `name_es` (cobertura 100%: Río de Janeiro,
    Columbia Británica…).
  - **Ciudades** (dataset "simple", sin campo en español) → override curado de ~51
    ciudades muy visibles (Tokyo→Tokio, Bogota→Bogotá, Mexico City→Ciudad de
    México, Munich→Múnich, Beijing→Pekín…).
  - Helper `esNombre()` + mapa `OVERRIDE_ES`. Verificado simulando la lógica contra
    los geojson reales y con `node --check`.

## Modificado

- **`src/com/ascendia/util/ConexionDB.java`**: lectura retrocompatible de
  `DB_URL`/`DB_USER`/`DB_PASS` por variables de entorno. Si no estan definidas,
  mantiene los valores por defecto del entorno local (la consola no cambia).
- **`web/.../public/app.js`** (`cargarDatos()`): ahora consume
  `GET /api/oportunidades` (datos desde MySQL) y conserva el array embebido como
  **respaldo offline** si la API no responde.
- **`README.md`** y **`.gitignore`**: seccion "Capa Web / Globo 3D" e ignorado de
  `web/target/`.

## Notas

- Las oportunidades del globo ya estaban sembradas en
  `database/ascendia_schema.sql` y `database/ascendia_database.sql` con
  coordenadas reales, por lo que la API las sirve desde MySQL (fuente unica de
  verdad) sin necesidad de un seed adicional.
- **Seed de universidades (capa de contexto del globo):** se cargaron en ambos
  seeds las **222 universidades del ranking QS 2026** (`universidades.js`) con su
  **nombre completo**, mas sus **39 paises**. Se conservan los IDs 1-10 originales
  (con los nombres corregidos a nombre completo: MIT, UNAM, "The University of
  Tokyo/Melbourne", USP) y se anexan las restantes con IDs 11-223. Las FK,
  oportunidades y consultas documentadas siguen validas. Validado cargando ambos
  archivos en MySQL: 39 paises, 223 universidades, 0 FK huerfanas.
- El nucleo evaluable (prototipo de consola, 29 clases) compila intacto.
- Build verificado a nivel de compilacion (`javac`); el empaquetado del fat jar
  requiere Maven (`mvn -q clean package` dentro de `web/`).
