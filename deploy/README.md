# Despliegue de Ascendia — Hugging Face Spaces (Docker)

Este documento describe cómo está desplegada la **vista web / globo 3D (RF11)** de
Ascendia en **Hugging Face Spaces** usando el SDK **Docker**, cómo probarla en
local y cómo recrearla desde cero.

> **URL pública (en vivo):** https://jesusfar-ascendia.hf.space
> **Página del Space:** https://huggingface.co/spaces/Jesusfar/ascendia
> Health check: `GET https://jesusfar-ascendia.hf.space/api/health` → `ok`

---

## 1. Por qué Hugging Face Spaces

- **Sin tarjeta de crédito** ni datos de pago (a diferencia de los free tiers de los
  proveedores cloud tradicionales).
- Soporta el **SDK Docker**: corremos exactamente la imagen que definimos, sin
  adaptar el proyecto a un PaaS específico.
- Publica automáticamente un único puerto HTTP (**7860**) detrás de HTTPS.

---

## 2. Arquitectura del deploy

Un **único contenedor** hace todo (no hay segundo host ni CORS):

```
┌───────────────────────────────────────────────────────────────┐
│  Contenedor HF Space (uid 1000, puerto 7860)                  │
│                                                                │
│   entrypoint.sh                                                │
│     1. inicializa y arranca  MySQL 8  (datadir propio)        │
│     2. crea base + usuario y siembra database/ascendia_schema.sql │
│     3. exec java -jar app.jar                                  │
│                                                                │
│   app.jar  (fat jar, Javalin/Jetty, JDK 21)                   │
│     ├─ sirve el globo 3D estático  (index.html, app.js, ...)  │
│     └─ expone la API REST  /api/oportunidades  (lee MySQL)    │
│                                                                │
│   MySQL 8.0  (127.0.0.1:3306, base "ascendia")                │
└───────────────────────────────────────────────────────────────┘
                         ▲ HTTPS :7860
                         │
                      Navegador
```

- El **fat jar** (Javalin sobre Jetty embebido) sirve **el globo + la API REST en el
  mismo puerto 7860**, por lo que el front hace `fetch('/api/oportunidades')` al
  mismo origen (sin CORS).
- **MySQL 8** corre **dentro** del mismo contenedor y se **siembra al iniciar** desde
  `database/ascendia_schema.sql` (deja exactamente las 6 oportunidades activas del
  globo).
- HF ejecuta el contenedor como **uid 1000 (no root)**; por eso `mysqld` corre con
  `--no-defaults` y un `datadir`/`socket` propios bajo `/home/user` (ver nota en
  §6).

### Imagen (multi-stage)

`Dockerfile` (en la raíz del repo):

- **Stage build** — `maven:3.9-eclipse-temurin-21`: compila el fat jar con
  `cd web && mvn -q -DskipTests clean package`. El módulo `web/` reutiliza `../src`
  (el núcleo de consola) vía *build-helper*, así que el JAR incluye los DAO/JDBC del
  prototipo sin duplicar código.
- **Stage runtime** — `eclipse-temurin:21-jre-jammy` + `mysql-server` (apt de Jammy
  trae MySQL 8.0): copia `app.jar`, `database/` y `deploy/entrypoint.sh`.

---

## 3. Variables de entorno

El JAR no tiene nada hardcodeado; lee la conexión por env (definidas en el
`Dockerfile`, sobreescribibles desde la UI del Space → *Settings → Variables*):

| Variable  | Valor en el deploy | Para qué sirve |
|-----------|--------------------|----------------|
| `PORT`    | `7860`             | Puerto HTTP que publica HF Spaces. |
| `DB_URL`  | `jdbc:mysql://127.0.0.1:3306/ascendia?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true` | JDBC a la base interna. |
| `DB_USER` | `ascendia`         | Usuario de la app (creado por el entrypoint). |
| `DB_PASS` | `ascendia`         | Password de ese usuario. |

En local, si no se definen, `ConexionDB` cae a los valores por defecto del entorno
de desarrollo (`root` sin clave), sin afectar al prototipo de consola.

---

## 4. Build y prueba en local (Docker)

Desde la **raíz** del repo (donde está el `Dockerfile`):

```bash
# 1. Construir la imagen (compila el fat jar e instala MySQL 8)
docker build -t ascendia .

# 2. Correr el contenedor mapeando el 7860
docker run --rm -p 7860:7860 ascendia

# 3. Abrir en el navegador
#    http://localhost:7860            -> globo 3D
#    http://localhost:7860/api/health -> ok
#    http://localhost:7860/api/oportunidades -> JSON de las 6 activas
```

El primer arranque tarda unos segundos: inicializa el datadir de MySQL, lo siembra y
recién entonces levanta el JAR.

---

## 5. Crear el Space y publicar (paso a paso)

1. **Crear el Space** en https://huggingface.co/new-space
   - *Owner*: `Jesusfar` · *Space name*: `ascendia`
   - *SDK*: **Docker** (plantilla *Blank*) · *Hardware*: CPU basic (gratis).
2. El **front-matter** del Space va en el `README.md` de la raíz (ya incluido):

   ```yaml
   ---
   title: Ascendia
   emoji: 🌍
   colorFrom: blue
   colorTo: indigo
   sdk: docker
   app_port: 7860
   pinned: false
   ---
   ```

   `app_port: 7860` le dice a HF qué puerto exponer.
3. **Agregar el remoto y pushear** (el repo del Space es git):

   ```bash
   git remote add space git@hf.co:spaces/Jesusfar/ascendia   # (ya configurado)
   git push space main
   ```
4. HF detecta el `Dockerfile`, **buildea y arranca** automáticamente. El progreso se
   ve en la pestaña *Logs* del Space. Cuando el estado pasa a **Running**, la app
   queda en `https://jesusfar-ascendia.hf.space`.

> Nota: el remoto `space` (`git@hf.co:spaces/Jesusfar/ascendia`) es independiente del
> remoto `origin` de GitHub. El código fuente vive en GitHub; el Space es solo el
> destino de despliegue.

---

## 6. Notas y limitaciones

- **Disco no persistente.** El sistema de archivos del contenedor es efímero: en cada
  arranque (o *factory reboot* del Space) MySQL se **reinicializa y se vuelve a
  sembrar** desde `database/ascendia_schema.sql`. La web es **solo lectura** (el globo
  consulta, no escribe), así que esto es correcto para la demo, pero **cualquier alta
  hecha sobre la base del Space se pierde** al reiniciar.
- **`mysqld --no-defaults`.** HF corre como uid 1000; el `/etc/mysql` del paquete
  fuerza `user=mysql` y `log-error=/var/log/mysql`, rutas que ese uid no puede usar.
  Por eso el entrypoint arranca MySQL con `--no-defaults` y datadir/socket propios bajo
  `/home/user`.
- **Seed = solo `ascendia_schema.sql`.** No se carga `ascendia_database.sql` (la
  versión documentada del TP2 hace DROP+CREATE y trae UPDATE/DELETE de demo que
  dejarían 8 activas); el schema operativo deja exactamente las **6** activas que
  espera el globo.

---

## 7. Variante futura: base persistente con Aiven

Para que las altas sobrevivan a los reinicios, la evolución natural es **separar la
base** del contenedor usando un MySQL gestionado gratuito como **Aiven for MySQL**
(free tier, sin tarjeta):

1. Crear un servicio MySQL en Aiven y obtener host, puerto, base, usuario y password
   (Aiven entrega también el certificado/SSL).
2. En el **Dockerfile/entrypoint**, dejar de instalar y sembrar MySQL local: el
   contenedor pasaría a correr **solo el JAR**.
3. Configurar en *Settings → Variables/Secrets* del Space:
   - `DB_URL = jdbc:mysql://<host-aiven>:<puerto>/ascendia?useSSL=true&...`
   - `DB_USER`, `DB_PASS` como **secrets** (no en el `Dockerfile`).
4. Sembrar el esquema **una sola vez** contra Aiven (no en cada arranque), con lo que
   los datos persisten entre reinicios del Space.

Ventaja: estado persistente y arranque más rápido (sin instalar/sembrar MySQL en cada
boot). Costo: una dependencia de red externa y gestionar credenciales como secrets.
