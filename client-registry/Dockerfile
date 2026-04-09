# ── Stage 1: build ───────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml /workspace/pom.xml
COPY client-registry/pom.xml /workspace/client-registry/pom.xml

RUN mvn -f /workspace/pom.xml -pl client-registry -am dependency:go-offline -q

COPY client-registry/src /workspace/client-registry/src
RUN mvn -f /workspace/pom.xml -pl client-registry -am package -DskipTests -q

# ── Stage 2: runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /workspace/client-registry/target/client-registry-*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
