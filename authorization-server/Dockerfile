# ── Stage 1: build ───────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml /workspace/pom.xml
COPY authorization-server/pom.xml /workspace/authorization-server/pom.xml

RUN mvn -f /workspace/pom.xml -pl authorization-server -am dependency:go-offline -q

COPY authorization-server/src /workspace/authorization-server/src
RUN mvn -f /workspace/pom.xml -pl authorization-server -am package -DskipTests -q

# ── Stage 2: runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /workspace/authorization-server/target/authorization-server-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
