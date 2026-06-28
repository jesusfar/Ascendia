# =====================================================================
# Ascendia — imagen unica para Hugging Face Spaces (SDK Docker).
# Un solo contenedor: el fat jar (Javalin) sirve el globo + la API REST,
# y MySQL 8 corre adentro y se siembra al iniciar. Sin CORS, sin 2do host.
# HF corre como uid 1000 y publica el puerto 7860; el disco no es persistente
# (la base se recrea desde el seed en cada arranque; la web es solo lectura).
# =====================================================================

# ---- Stage build: compila el fat jar con Maven + JDK 21 ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN cd web && mvn -q -DskipTests clean package

# ---- Stage runtime: JRE 21 + MySQL 8 (jammy trae MySQL 8.0 en apt) ----
FROM eclipse-temurin:21-jre-jammy
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -y --no-install-recommends mysql-server \
    && rm -rf /var/lib/apt/lists/*
RUN useradd -m -u 1000 user
WORKDIR /home/user/app
COPY --from=build /app/web/target/ascendia-web.jar app.jar
COPY database/ ./database/
COPY deploy/entrypoint.sh ./entrypoint.sh
RUN mkdir -p /home/user/mysql-data /home/user/mysql-run \
    && chown -R 1000:1000 /home/user
USER 1000
ENV PORT=7860 \
    DB_URL="jdbc:mysql://127.0.0.1:3306/ascendia?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true" \
    DB_USER=ascendia DB_PASS=ascendia
EXPOSE 7860
ENTRYPOINT ["bash","entrypoint.sh"]
