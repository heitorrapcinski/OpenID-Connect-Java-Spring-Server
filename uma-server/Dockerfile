# ── Stage 1: build ───────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml /workspace/pom.xml
COPY scope-manager/pom.xml /workspace/scope-manager/pom.xml
COPY client-registry/pom.xml /workspace/client-registry/pom.xml
COPY authorization-server/pom.xml /workspace/authorization-server/pom.xml
COPY oidc-provider/pom.xml /workspace/oidc-provider/pom.xml
COPY uma-server/pom.xml /workspace/uma-server/pom.xml
COPY api-gateway/pom.xml /workspace/api-gateway/pom.xml
COPY migration-tool/pom.xml /workspace/migration-tool/pom.xml

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
