# ── Stage 1: build ───────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy all module POMs for dependency resolution (multi-module project)
COPY pom.xml /workspace/pom.xml
COPY scope-manager/pom.xml /workspace/scope-manager/pom.xml
COPY client-registry/pom.xml /workspace/client-registry/pom.xml
COPY authorization-server/pom.xml /workspace/authorization-server/pom.xml
COPY oidc-provider/pom.xml /workspace/oidc-provider/pom.xml
COPY uma-server/pom.xml /workspace/uma-server/pom.xml
COPY api-gateway/pom.xml /workspace/api-gateway/pom.xml
COPY migration-tool/pom.xml /workspace/migration-tool/pom.xml

# Resolve dependencies (cached layer)
RUN mvn -f /workspace/pom.xml -pl scope-manager -am dependency:go-offline -q

# Copy source and build
COPY scope-manager/src /workspace/scope-manager/src
RUN mvn -f /workspace/pom.xml -pl scope-manager -am package -DskipTests -q

# ── Stage 2: runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /workspace/scope-manager/target/scope-manager-*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
