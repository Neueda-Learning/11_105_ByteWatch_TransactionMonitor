# ---------- Build Stage ----------
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy the POM first so Docker can cache the dependency layer separately
# from source changes — re-running `mvn` only re-downloads deps when
# pom.xml itself changes, not on every code edit.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
# Tests already ran as their own Jenkins stage (see Jenkinsfile) before this
# image is built, so skip re-running them here to keep the image build fast.
RUN mvn -B clean package -DskipTests

# ---------- Runtime Stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# eclipse-temurin's JRE image doesn't ship curl/wget — install curl (small,
# needed for the HEALTHCHECK below) and clean up apt caches in the same
# layer to avoid bloating the image.
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Run as a non-root user rather than the container default root.
RUN addgroup --system spring && adduser --system --ingroup spring spring
COPY --from=builder /app/target/*.jar app.jar
RUN chown spring:spring app.jar
USER spring

EXPOSE 8080

# Relies on spring-boot-starter-actuator (already in pom.xml) exposing
# /actuator/health. Lets `docker ps` / compose show real container health
# instead of just "process is running".
HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
    CMD curl -fs http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]