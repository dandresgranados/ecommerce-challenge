# ============================================================
# Dockerfile del backend (Spring Boot 4)
# Multi-stage build:
#   1. builder  → compila con Maven (imagen pesada, se descarta)
#   2. runtime  → solo JRE + JAR ejecutable (imagen ligera final)
# ============================================================

# --------- STAGE 1: BUILD ---------
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copiar solo el pom primero para aprovechar la caché de Docker:
# si el pom no cambia, Docker reutiliza la capa con las dependencias
# ya descargadas en builds posteriores (el propio "package" las descarga).
COPY pom.xml .

# Copiar el código y compilar (saltando tests: se ejecutan en el pipeline
# de backend-ci.yml aparte).
# Sin -q para ver los errores completos si algo falla en el CI.
COPY src ./src
# Se saltan Checkstyle/SpotBugs en la imagen Docker porque:
#   - Ya se ejecutan en el pipeline backend-ci.yml (job static-analysis).
#   - Sus archivos de config (checkstyle.xml, spotbugs-exclude.xml) no
#     se copian al contexto para mantener la imagen mínima.
RUN mvn -B -ntp clean package -DskipTests -Dcheckstyle.skip -Dspotbugs.skip

# --------- STAGE 2: RUNTIME ---------
FROM eclipse-temurin:17-jre AS runtime

# Usuario no-root para reducir superficie de ataque.
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

WORKDIR /app

# Copia solo el JAR resultante del stage anterior — no arrastra Maven ni fuentes.
COPY --from=builder --chown=spring:spring /build/target/*.jar app.jar

# Puerto donde escucha Tomcat embebido.
EXPOSE 8080

# JVM tuning para contenedores:
#   -XX:MaxRAMPercentage=75  → usa hasta el 75 % de la memoria del contenedor
#   -Djava.security.egd=file:/dev/./urandom → arranque más rápido de SecureRandom
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
