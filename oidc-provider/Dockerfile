# ── Stage 1: build ───────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml /workspace/pom.xml
COPY oidc-provider/pom.xml /workspace/oidc-provider/pom.xml

RUN mvn -f /workspace/pom.xml -pl oidc-provider -am dependency:go-offline -q

COPY oidc-provider/src /workspace/oidc-provider/src
RUN mvn -f /workspace/pom.xml -pl oidc-provider -am package -DskipTests -q

# ── Stage 2: runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /workspace/oidc-provider/target/oidc-provider-*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
