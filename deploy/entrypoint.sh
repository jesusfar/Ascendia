#!/usr/bin/env bash
# =====================================================================
# Entrypoint del contenedor de Ascendia (HF Spaces).
# Corre como uid 1000 (no root): inicia MySQL 8 con datadir propio,
# crea la base + usuario de la app, siembra el esquema y arranca el jar.
# El jar (Javalin) lee DB_URL/DB_USER/DB_PASS/PORT por env (no hardcode).
# =====================================================================
set -e
DATADIR=/home/user/mysql-data
RUNDIR=/home/user/mysql-run
SOCK=$RUNDIR/mysqld.sock
mkdir -p "$RUNDIR"

# init del datadir si esta vacio (crea root@localhost sin clave)
if [ ! -d "$DATADIR/mysql" ]; then
  mysqld --initialize-insecure --datadir="$DATADIR"
fi

# arrancar MySQL en background, como el usuario actual (sin --user)
mysqld --datadir="$DATADIR" --socket="$SOCK" --port=3306 --bind-address=127.0.0.1 \
       --pid-file="$RUNDIR/mysqld.pid" --innodb-buffer-pool-size=128M \
       --secure-file-priv="" &

# esperar a que levante
until mysqladmin --socket="$SOCK" ping --silent 2>/dev/null; do sleep 1; done

# crear base + usuario de la app (native_password para evitar lios de auth por TCP)
mysql --socket="$SOCK" -uroot <<'SQL'
CREATE DATABASE IF NOT EXISTS ascendia CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'ascendia'@'127.0.0.1' IDENTIFIED WITH mysql_native_password BY 'ascendia';
CREATE USER IF NOT EXISTS 'ascendia'@'localhost' IDENTIFIED WITH mysql_native_password BY 'ascendia';
GRANT ALL PRIVILEGES ON ascendia.* TO 'ascendia'@'127.0.0.1';
GRANT ALL PRIVILEGES ON ascendia.* TO 'ascendia'@'localhost';
FLUSH PRIVILEGES;
SQL

# Seed: SOLO el esquema operativo -> deja exactamente las 6 oportunidades
# activas del globo (ids 1,2,3,5,6,7), coherente con lo que muestra la web.
# database/ascendia_database.sql se mantiene en el repo como version
# documentada del TP2 (hace DROP+CREATE y trae UPDATE/DELETE de demo, por lo
# que dejaria 8 activas); NO se carga aca para no romper el gate de las 6.
mysql --socket="$SOCK" -uroot ascendia < database/ascendia_schema.sql

# arrancar la app (la web apunta a 127.0.0.1:3306 via DB_URL)
exec java -Xmx512m -jar app.jar
