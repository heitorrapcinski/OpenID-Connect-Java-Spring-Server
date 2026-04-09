# ── Stage 1: build ───────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml /workspace/pom.xml
COPY uma-server/pom.xml /workspace/uma-server/pom.xml

RUN mvn -f /workspace/pom.xml -pl uma-server -am dependency:go-offline -q

COPY uma-server/src /workspace/uma-server/src
RUN mvn -f /workspace/pom.xml -pl uma-server -am package -DskipTests -q

# ── Stage 2: runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /workspace/uma-server/target/uma-server-*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
