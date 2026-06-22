# ── Stage 1: build ──────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Pre-download dependencies (cached unless pom.xml changes)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src/ src/
RUN mvn package -DskipTests -B

# ── Stage 2: runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre AS runtime

RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
