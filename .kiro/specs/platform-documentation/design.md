# Design — Documentação da Plataforma (README.md)

## Visão Geral

Este documento descreve a estrutura e o conteúdo do `README.md` que será gerado na raiz do projeto. O objetivo é produzir um único arquivo de documentação que cubra quatro necessidades principais:

1. **Setup local** — como subir a plataforma via Docker Compose
2. **Referência de API** — endpoints principais de cada microsserviço com exemplos `curl`
3. **Guias de integração** — fluxos OAuth2/OIDC/UMA passo a passo para desenvolvedores de webapp
4. **Guia para novos integrantes** — arquitetura hexagonal, organização DDD, estrutura de pacotes e convenções

O `README.md` é um artefato estático gerado uma vez e mantido manualmente. Não há geração automática de código — o output desta spec é o próprio arquivo `README.md`.

---

## Arquitetura

O `README.md` é organizado em seções independentes e navegáveis. A estrutura segue a progressão natural de um desenvolvedor: entender o sistema → subir localmente → usar os endpoints → integrar uma webapp.

### Estrutura de Seções

```
README.md
├── 1. Visão Geral da Plataforma
│   ├── Introdução (propósito, DDD, hexagonal)
│   ├── Tabela de microsserviços
│   ├── Diagrama de arquitetura (Mermaid)
│   └── Pré-requisitos
├── 2. Como Subir a Plataforma
│   ├── Passos sequenciais (clone → build → up)
│   ├── Comandos de operação (ps, down, rebuild)
│   ├── Health checks
│   ├── Migration Tool
│   └── Portas de infraestrutura
├── 3. Referência de Endpoints
│   ├── API Gateway (base URL)
│   ├── Authorization Server
│   ├── Client Registry
│   ├── OIDC Provider
│   ├── UMA Server
│   └── Scope Manager
├── 4. Integração de Webapp — Authorization Code + PKCE
│   ├── Registro do cliente público
│   ├── Geração de code_verifier / code_challenge
│   ├── URL de autorização
│   ├── Troca do código pelo token
│   ├── Chamada ao /userinfo
│   └── Renovação via refresh_token
├── 5. Integração de Webapp — Client Credentials
│   ├── Registro do cliente confidencial
│   ├── Obtenção do access_token
│   └── Uso do token em APIs protegidas
├── 6. Integração de Webapp — UMA 2.0
│   ├── Papéis e conceitos
│   ├── Registro de ResourceSet
│   ├── Criação de PermissionTicket
│   ├── Obtenção do RPT
│   └── Validação do RPT via introspect
├── 7. Variáveis de Ambiente
│   ├── Authorization Server
│   ├── Client Registry
│   ├── OIDC Provider
│   ├── UMA Server
│   ├── Scope Manager
│   ├── API Gateway
│   └── Migration Tool
├── 8. Observabilidade
│   ├── Endpoints Actuator
│   ├── Formato de logs JSON
│   ├── Métricas Prometheus
│   └── Script de health check
└── 9. Guia para Novos Integrantes
    ├── Arquitetura Hexagonal (Ports & Adapters)
    │   ├── As três camadas: domain, application, infrastructure
    │   ├── Diagrama de fluxo de dependência (Mermaid)
    │   └── Regra de dependência
    ├── Organização DDD de Cada Microsserviço
    │   ├── domain/model/ — entidades e agregados
    │   ├── domain/model/vo/ — Value Objects
    │   ├── domain/port/in/ — interfaces de casos de uso
    │   ├── domain/port/out/ — interfaces de saída
    │   ├── domain/event/ — eventos de domínio
    │   ├── domain/exception/ — exceções de domínio
    │   └── application/service/ — implementações de casos de uso
    ├── Estrutura de Pacotes Padrão (authorization-server como exemplo)
    │   ├── Árvore completa de pacotes
    │   ├── infrastructure/adapter/in/web/ — controllers REST
    │   ├── infrastructure/adapter/out/persistence/ — repositórios MongoDB
    │   ├── infrastructure/adapter/out/messaging/ — publishers Kafka
    │   ├── infrastructure/adapter/out/rest/ — clientes HTTP
    │   └── infrastructure/config/ — classes de configuração
    └── Convenções e Decisões Arquiteturais
        ├── Isolamento de dados por microsserviço
        ├── Comunicação assíncrona via Kafka
        ├── Comunicação síncrona via adaptadores REST
        ├── Entidades de domínio como POJOs
        ├── Value Objects imutáveis
        ├── Exceções de domínio e GlobalExceptionHandler
        └── Estrutura Maven
```

---

## Componentes e Interfaces

### Seção 1 — Visão Geral da Plataforma

**Introdução:** Parágrafo descrevendo a plataforma como uma implementação de referência de microsserviços OIDC/OAuth2/UMA, construída com DDD e arquitetura hexagonal (Ports & Adapters), usando Spring Boot 3, MongoDB e Kafka.

**Tabela de microsserviços:**

| Serviço | Porta | Responsabilidade |
|---|---|---|
| `api-gateway` | 8090 | Roteamento público, ponto de entrada único |
| `authorization-server` | 8080 | Fluxos OAuth 2.0, emissão/revogação de tokens |
| `client-registry` | 8081 | Registro dinâmico de clientes (RFC 7591/7592) |
| `oidc-provider` | 8082 | UserInfo, ID Token, Discovery, JWKS |
| `uma-server` | 8083 | UMA 2.0: ResourceSet, PermissionTicket, RPT |
| `scope-manager` | 8084 | Gerenciamento de SystemScopes |
| `migration-tool` | — | Migração run-once de dados relacionais para MongoDB |

**Diagrama de arquitetura (Mermaid):** Diagrama `graph TB` mostrando o cliente externo → API Gateway → microsserviços → MongoDB (5 instâncias) e Kafka. Inclui as dependências de startup (scope-manager primeiro, depois client-registry e authorization-server, depois oidc-provider e uma-server, por último api-gateway).

**Pré-requisitos:**
- Docker 24+
- Docker Compose v2.20+
- Java 21 + Maven 3.9 (para build local dos JARs)

---

### Seção 2 — Como Subir a Plataforma

Passos sequenciais numerados:

1. `git clone <repo-url> && cd <repo>`
2. `mvn clean package -DskipTests` — compila todos os módulos e gera os JARs necessários para os Dockerfiles
3. `docker-compose up --build -d` — constrói as imagens e sobe todos os containers em background

Comandos de operação:

```bash
# Verificar status dos containers
docker-compose ps

# Parar e remover containers
docker-compose down

# Reconstruir apenas um serviço específico
docker-compose up --build -d <nome-do-serviço>

# Subir a Migration Tool (profile separado)
docker-compose --profile migration up migration-tool
```

Health checks de cada serviço (formato `GET http://localhost:{porta}/actuator/health`):

```bash
curl http://localhost:8090/actuator/health  # api-gateway
curl http://localhost:8080/actuator/health  # authorization-server
curl http://localhost:8081/actuator/health  # client-registry
curl http://localhost:8082/actuator/health  # oidc-provider
curl http://localhost:8083/actuator/health  # uma-server
curl http://localhost:8084/actuator/health  # scope-manager
```

Portas de infraestrutura:

| Serviço | Porta Host |
|---|---|
| mongodb-auth | 27017 |
| mongodb-client | 27018 |
| mongodb-oidc | 27019 |
| mongodb-uma | 27020 |
| mongodb-scope | 27021 |
| kafka | 9092 |

---

### Seção 3 — Referência de Endpoints

Todos os endpoints públicos são acessíveis via API Gateway em `http://localhost:8090`. Os exemplos `curl` usam esta base URL. Endpoints que exigem autenticação incluem o header `Authorization: Bearer {access_token}` ou autenticação Basic via `-u client_id:client_secret`.

#### Authorization Server (`/authorize`, `/token`, `/revoke`, `/introspect`)

Exemplos para cada grant type do `POST /token`:

- `authorization_code` com PKCE
- `client_credentials` com Basic auth
- `refresh_token`

#### Client Registry (`/register`, `/register/{client_id}`)

Exemplos de corpo de requisição para `POST /register` e resposta `201` com `client_id`, `client_secret` e `registration_access_token`.

#### OIDC Provider (`/userinfo`, `/.well-known/openid-configuration`, `/jwks`)

Exemplo de resposta do discovery document e do endpoint `/userinfo`.

#### UMA Server (`/uma/resource_set`, `/uma/permission`)

Exemplos de registro de ResourceSet e criação de PermissionTicket.

#### Scope Manager (`/scopes`, `/scopes/defaults`)

Exemplos de listagem e criação de escopos.

---

### Seção 4 — Authorization Code + PKCE

Fluxo completo em 6 passos:

**Passo 1 — Registrar cliente público:**
```bash
curl -X POST http://localhost:8090/register \
  -H "Content-Type: application/json" \
  -d '{
    "client_name": "My SPA",
    "redirect_uris": ["http://localhost:3000/callback"],
    "grant_types": ["authorization_code", "refresh_token"],
    "response_types": ["code"],
    "token_endpoint_auth_method": "none",
    "scope": "openid profile email"
  }'
```

**Passo 2 — Gerar code_verifier e code_challenge (JavaScript):**
```javascript
// Gerar code_verifier (43-128 chars, URL-safe)
const array = new Uint8Array(32);
crypto.getRandomValues(array);
const codeVerifier = btoa(String.fromCharCode(...array))
  .replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');

// Gerar code_challenge (SHA-256 + Base64URL)
const encoder = new TextEncoder();
const data = encoder.encode(codeVerifier);
const digest = await crypto.subtle.digest('SHA-256', data);
const codeChallenge = btoa(String.fromCharCode(...new Uint8Array(digest)))
  .replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
```

**Passo 3 — Construir URL de autorização:**
```
http://localhost:8090/authorize
  ?response_type=code
  &client_id={client_id}
  &redirect_uri=http://localhost:3000/callback
  &scope=openid%20profile%20email
  &state={random_state}
  &code_challenge={code_challenge}
  &code_challenge_method=S256
```

**Passo 4 — Trocar código pelo token:**
```bash
curl -X POST http://localhost:8090/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code" \
  -d "code={authorization_code}" \
  -d "redirect_uri=http://localhost:3000/callback" \
  -d "client_id={client_id}" \
  -d "code_verifier={code_verifier}"
```

**Passo 5 — Chamar /userinfo:**
```bash
curl http://localhost:8090/userinfo \
  -H "Authorization: Bearer {access_token}"
```

**Passo 6 — Renovar token:**
```bash
curl -X POST http://localhost:8090/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=refresh_token" \
  -d "refresh_token={refresh_token}" \
  -d "client_id={client_id}"
```

---

### Seção 5 — Client Credentials

**Passo 1 — Registrar cliente confidencial:**
```bash
curl -X POST http://localhost:8090/register \
  -H "Content-Type: application/json" \
  -d '{
    "client_name": "My Backend Service",
    "grant_types": ["client_credentials"],
    "token_endpoint_auth_method": "client_secret_basic",
    "scope": "read write"
  }'
```

**Passo 2 — Obter access_token:**
```bash
curl -X POST http://localhost:8090/token \
  -u "{client_id}:{client_secret}" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "scope=read write"
```

**Passo 3 — Introspectar token:**
```bash
curl -X POST http://localhost:8090/introspect \
  -u "{client_id}:{client_secret}" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "token={access_token}"
```

---

### Seção 6 — UMA 2.0

**Papéis:**
- **Resource Owner** — usuário que possui os recursos
- **Resource Server** — serviço que hospeda os recursos protegidos
- **Requesting Party** — usuário ou serviço que solicita acesso
- **Authorization Server** — emite tokens e gerencia políticas

**Passo 1 — Registrar ResourceSet (Resource Server):**
```bash
curl -X POST http://localhost:8090/uma/resource_set \
  -H "Authorization: Bearer {resource_owner_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Photos",
    "uri": "https://photos.example.com/album/1",
    "type": "http://www.example.com/rsrcs/photoalbum",
    "resource_scopes": ["read", "write"],
    "icon_uri": "https://photos.example.com/icon.png"
  }'
```

**Passo 2 — Criar PermissionTicket (Resource Server, ao receber requisição sem token):**
```bash
curl -X POST http://localhost:8090/uma/permission \
  -H "Authorization: Bearer {resource_server_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "resource_id": "{resource_set_id}",
    "resource_scopes": ["read"]
  }'
# Resposta: { "ticket": "016f84e8-..." }
```

**Passo 3 — Obter RPT (Requesting Party):**
```bash
curl -X POST http://localhost:8090/token \
  -u "{client_id}:{client_secret}" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=urn:ietf:params:oauth:grant-type:uma-ticket" \
  -d "ticket={permission_ticket}"
```

**Passo 4 — Validar RPT (Resource Server):**
```bash
curl -X POST http://localhost:8090/introspect \
  -u "{client_id}:{client_secret}" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "token={rpt}"
```

---

### Seção 7 — Variáveis de Ambiente

Tabelas por microsserviço com colunas: Variável | Descrição | Valor Padrão (docker-compose) | Obrigatória.

**Authorization Server:**

| Variável | Descrição | Padrão | Obrigatória |
|---|---|---|---|
| `SPRING_DATA_MONGODB_URI` | URI de conexão MongoDB | `mongodb://mongodb-auth:27017/auth_db` | Sim |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Endereço do broker Kafka | `kafka:9092` | Sim |
| `SERVER_PORT` | Porta HTTP do serviço | `8080` | Sim |
| `SCOPE_MANAGER_URL` | URL base do scope-manager | `http://scope-manager:8084` | Sim |
| `CLIENT_REGISTRY_URL` | URL base do client-registry | `http://client-registry:8081` | Sim |

**API Gateway:**

| Variável | Descrição | Padrão | Obrigatória |
|---|---|---|---|
| `SERVER_PORT` | Porta HTTP do gateway | `8090` | Sim |
| `AUTHORIZATION_SERVER_URL` | URL do authorization-server | `http://authorization-server:8080` | Sim |
| `CLIENT_REGISTRY_URL` | URL do client-registry | `http://client-registry:8081` | Sim |
| `OIDC_PROVIDER_URL` | URL do oidc-provider | `http://oidc-provider:8082` | Sim |
| `UMA_SERVER_URL` | URL do uma-server | `http://uma-server:8083` | Sim |
| `SCOPE_MANAGER_URL` | URL do scope-manager | `http://scope-manager:8084` | Sim |

**Migration Tool:**

| Variável | Descrição | Padrão | Obrigatória |
|---|---|---|---|
| `MONGODB_AUTH_URI` | URI MongoDB do authorization-server | `mongodb://mongodb-auth:27017/auth_db` | Sim |
| `MONGODB_CLIENT_URI` | URI MongoDB do client-registry | `mongodb://mongodb-client:27017/client_db` | Sim |
| `MONGODB_OIDC_URI` | URI MongoDB do oidc-provider | `mongodb://mongodb-oidc:27017/oidc_db` | Sim |
| `MONGODB_UMA_URI` | URI MongoDB do uma-server | `mongodb://mongodb-uma:27017/uma_db` | Sim |
| `MONGODB_SCOPE_URI` | URI MongoDB do scope-manager | `mongodb://mongodb-scope:27017/scope_db` | Sim |

---

### Seção 8 — Observabilidade

**Endpoints Actuator** (disponíveis em todos os microsserviços):

| Endpoint | Descrição |
|---|---|
| `GET /actuator/health` | Status de saúde do serviço e dependências |
| `GET /actuator/metrics` | Métricas no formato Micrometer |
| `GET /actuator/info` | Informações de build e versão |

**Formato de log JSON** (campos presentes em cada linha de log):

```json
{
  "timestamp": "2024-01-01T00:00:00.000Z",
  "level": "INFO",
  "service": "authorization-server",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736",
  "spanId": "00f067aa0ba902b7",
  "message": "Token issued for client my-client"
}
```

**Configuração de scrape Prometheus:**

```yaml
scrape_configs:
  - job_name: 'oidc-platform'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets:
          - 'localhost:8080'  # authorization-server
          - 'localhost:8081'  # client-registry
          - 'localhost:8082'  # oidc-provider
          - 'localhost:8083'  # uma-server
          - 'localhost:8084'  # scope-manager
          - 'localhost:8090'  # api-gateway
```

**Script de health check de todos os serviços:**

```bash
#!/bin/bash
SERVICES=(
  "api-gateway:8090"
  "authorization-server:8080"
  "client-registry:8081"
  "oidc-provider:8082"
  "uma-server:8083"
  "scope-manager:8084"
)

for entry in "${SERVICES[@]}"; do
  name="${entry%%:*}"
  port="${entry##*:}"
  status=$(curl -s -o /dev/null -w "%{http_code}" \
    "http://localhost:${port}/actuator/health")
  echo "${name}: HTTP ${status}"
done
```

---

### Seção 9 — Guia para Novos Integrantes

#### 9.1 Arquitetura Hexagonal (Ports & Adapters)

Esta subseção explica o padrão arquitetural adotado em todos os microsserviços da plataforma.

**As três camadas:**

- **`domain`** — núcleo de negócio puro. Contém entidades, value objects, eventos, exceções e interfaces de porta (`port/in`, `port/out`). Não possui nenhuma dependência de framework (sem Spring, sem MongoDB, sem Kafka). É a camada mais interna e estável.
- **`application`** — orquestração de casos de uso. Contém as implementações das interfaces `port/in` definidas no domínio (`application/service/`). Coordena chamadas ao domínio e às portas de saída. Pode depender do `domain`, mas não de `infrastructure`.
- **`infrastructure`** — adaptadores de entrada e saída. Contém os controllers REST (`adapter/in/web/`), repositórios MongoDB (`adapter/out/persistence/`), publishers Kafka (`adapter/out/messaging/`), clientes HTTP (`adapter/out/rest/`) e configurações Spring (`config/`). Implementa as interfaces `port/out` definidas no domínio.

**Diagrama de fluxo de dependência (Mermaid):**

```mermaid
graph LR
    subgraph infrastructure
        IN[adapter/in/web\nTokenEndpoint]
        OUT_P[adapter/out/persistence\nMongoAccessTokenRepository]
        OUT_M[adapter/out/messaging\nKafkaDomainEventPublisher]
        OUT_R[adapter/out/rest\nClientRegistryRestAdapter]
    end

    subgraph application
        SVC[service\nIssueTokenService]
    end

    subgraph domain
        PORT_IN[port/in\nIssueTokenUseCase]
        PORT_OUT[port/out\nAccessTokenRepository\nDomainEventPublisher\nClientQueryPort]
        MODEL[model\nAccessToken\nRefreshToken]
        VO[model/vo\nTokenValue\nClientId]
        EVENT[event\nAccessTokenIssued]
        EX[exception\nInvalidGrantException]
    end

    IN -->|chama| SVC
    SVC -->|implementa| PORT_IN
    SVC -->|usa| PORT_OUT
    SVC -->|usa| MODEL
    OUT_P -->|implementa| PORT_OUT
    OUT_M -->|implementa| PORT_OUT
    OUT_R -->|implementa| PORT_OUT
```

**Regra de dependência:** camadas externas dependem de camadas internas, nunca o contrário. O `domain` não conhece nenhuma tecnologia de infraestrutura. A `infrastructure` depende do `domain` (via interfaces `port/out`). A `application` depende do `domain` (via interfaces `port/in` e `port/out`). Isso garante que o núcleo de negócio pode ser testado isoladamente, sem necessidade de Spring, MongoDB ou Kafka.

---

#### 9.2 Organização DDD de Cada Microsserviço

Cada microsserviço organiza seu código em subpacotes dentro de `domain/` com propósitos bem definidos. Os exemplos abaixo usam o `authorization-server` como referência.

**`domain/model/`** — entidades e agregados do domínio. São POJOs sem anotações de framework.
- Exemplos: `AccessToken`, `RefreshToken`, `AuthorizationCode`

**`domain/model/vo/`** — Value Objects imutáveis que encapsulam conceitos do domínio com validação própria.
- Exemplos: `ClientId`, `TokenValue`, `PKCEChallenge`, `Scope`

**`domain/port/in/`** — interfaces de casos de uso. Cada interface representa uma intenção de negócio e é implementada por `application/service/`.
- Exemplos: `IssueTokenUseCase`, `RevokeTokenUseCase`, `IntrospectTokenUseCase`

**`domain/port/out/`** — interfaces de saída do domínio. Abstraem persistência e comunicação externa, implementadas por `infrastructure/adapter/out/`.
- Exemplos: `AccessTokenRepository`, `DomainEventPublisher`, `ClientQueryPort`, `ScopeQueryPort`

**`domain/event/`** — eventos de domínio publicados após operações relevantes, enviados ao Kafka via `KafkaDomainEventPublisher`.
- Exemplos: `AccessTokenIssued`, `AccessTokenRevoked`, `RefreshTokenIssued`

**`domain/exception/`** — exceções tipadas que representam violações de regras de negócio. Estendem `DomainException` e são mapeadas para respostas HTTP pelo `GlobalExceptionHandler`.
- Exemplos: `InvalidGrantException`, `ClientNotFoundException`, `InvalidScopeException`

**`application/service/`** — implementações dos casos de uso definidos em `domain/port/in/`. Orquestram chamadas ao domínio e às portas de saída.
- Exemplos: `IssueTokenService`, `RevokeTokenService`, `IntrospectTokenService`

---

#### 9.3 Estrutura de Pacotes Padrão — `authorization-server` como Exemplo Canônico

Ao criar ou navegar em qualquer microsserviço, a estrutura de pacotes segue este padrão. O `authorization-server` é o exemplo mais completo:

```
authorization-server/
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/example/authserver/
    │   │       ├── domain/
    │   │       │   ├── model/
    │   │       │   │   ├── AccessToken.java
    │   │       │   │   ├── RefreshToken.java
    │   │       │   │   ├── AuthorizationCode.java
    │   │       │   │   └── vo/
    │   │       │   │       ├── TokenValue.java
    │   │       │   │       ├── ClientId.java
    │   │       │   │       ├── PKCEChallenge.java
    │   │       │   │       └── Scope.java
    │   │       │   ├── port/
    │   │       │   │   ├── in/
    │   │       │   │   │   ├── IssueTokenUseCase.java
    │   │       │   │   │   ├── RevokeTokenUseCase.java
    │   │       │   │   │   └── IntrospectTokenUseCase.java
    │   │       │   │   └── out/
    │   │       │   │       ├── AccessTokenRepository.java
    │   │       │   │       ├── RefreshTokenRepository.java
    │   │       │   │       ├── DomainEventPublisher.java
    │   │       │   │       ├── ClientQueryPort.java
    │   │       │   │       └── ScopeQueryPort.java
    │   │       │   ├── event/
    │   │       │   │   ├── AccessTokenIssued.java
    │   │       │   │   ├── AccessTokenRevoked.java
    │   │       │   │   └── RefreshTokenIssued.java
    │   │       │   └── exception/
    │   │       │       ├── DomainException.java
    │   │       │       ├── InvalidGrantException.java
    │   │       │       ├── ClientNotFoundException.java
    │   │       │       └── InvalidScopeException.java
    │   │       ├── application/
    │   │       │   └── service/
    │   │       │       ├── IssueTokenService.java
    │   │       │       ├── RevokeTokenService.java
    │   │       │       └── IntrospectTokenService.java
    │   │       └── infrastructure/
    │   │           ├── adapter/
    │   │           │   ├── in/
    │   │           │   │   └── web/
    │   │           │   │       ├── TokenEndpoint.java
    │   │           │   │       ├── AuthorizationEndpoint.java
    │   │           │   │       ├── IntrospectionEndpoint.java
    │   │           │   │       ├── RevocationEndpoint.java
    │   │           │   │       └── GlobalExceptionHandler.java
    │   │           │   └── out/
    │   │           │       ├── persistence/
    │   │           │       │   ├── MongoAccessTokenRepository.java
    │   │           │       │   ├── SpringDataAccessTokenRepository.java
    │   │           │       │   ├── MongoRefreshTokenRepository.java
    │   │           │       │   ├── SpringDataRefreshTokenRepository.java
    │   │           │       │   └── document/
    │   │           │       │       ├── AccessTokenDocument.java
    │   │           │       │       └── RefreshTokenDocument.java
    │   │           │       ├── messaging/
    │   │           │       │   └── KafkaDomainEventPublisher.java
    │   │           │       └── rest/
    │   │           │           ├── ClientRegistryRestAdapter.java
    │   │           │           └── ScopeManagerRestAdapter.java
    │   │           └── config/
    │   │               ├── MongoConfig.java
    │   │               ├── KafkaConfig.java
    │   │               ├── SecurityConfig.java
    │   │               └── JwkConfig.java
    │   └── resources/
    │       └── application.yml
    └── test/
        └── java/
            └── com/example/authserver/
                └── (espelha a estrutura de src/main/java)
```

**Convenções de nomenclatura:**
- Adaptadores de persistência: `Mongo{Entidade}Repository` para a implementação, `SpringData{Entidade}Repository` para a interface Spring Data subjacente.
- Documentos MongoDB: `{Entidade}Document` em `persistence/document/`, com anotações `@Document` e mapeamento explícito de/para a entidade de domínio.
- Controllers REST: nomeados pelo endpoint que expõem (ex: `TokenEndpoint`, `AuthorizationEndpoint`), não pelo padrão `*Controller`.

Ao criar um novo microsserviço, replique esta mesma estrutura de pacotes `domain/`, `application/` e `infrastructure/` com os mesmos subpacotes.

---

#### 9.4 Convenções e Decisões Arquiteturais

Estas convenções se aplicam a todos os microsserviços da plataforma e devem ser seguidas ao contribuir com código novo.

**Isolamento de dados por microsserviço:** cada microsserviço possui seu próprio banco de dados MongoDB dedicado. Nenhum microsserviço acessa diretamente o banco de dados de outro. A comunicação entre serviços é feita exclusivamente via APIs ou eventos.

**Comunicação assíncrona via Kafka:** eventos de domínio definidos em `domain/event/` são publicados no Kafka pelo `KafkaDomainEventPublisher` (`infrastructure/adapter/out/messaging/`). Outros microsserviços consomem esses eventos para reagir a mudanças de estado sem acoplamento direto.

**Comunicação síncrona via adaptadores REST:** quando um microsserviço precisa consultar outro de forma síncrona (ex: `authorization-server` consultando `client-registry` e `scope-manager`), isso é feito via adaptadores REST em `infrastructure/adapter/out/rest/` (ex: `ClientRegistryRestAdapter`, `ScopeManagerRestAdapter`). Esses adaptadores implementam interfaces de porta de saída definidas no domínio (`ClientQueryPort`, `ScopeQueryPort`), mantendo o domínio desacoplado da tecnologia HTTP.

**Entidades de domínio são POJOs:** as classes em `domain/model/` não possuem anotações de framework (`@Document`, `@Entity`, `@JsonProperty`). O mapeamento para documentos MongoDB é feito nas classes `*Document` em `infrastructure/adapter/out/persistence/document/`, que são responsáveis pela conversão de/para as entidades de domínio.

**Value Objects são imutáveis:** as classes em `domain/model/vo/` possuem campos `final`, sem setters, e encapsulam validação e semântica do conceito que representam. A validação é feita no construtor — um Value Object inválido nunca é criado.

**Exceções de domínio e GlobalExceptionHandler:** exceções em `domain/exception/` estendem a classe base `DomainException`. O `GlobalExceptionHandler` em `infrastructure/adapter/in/web/` captura essas exceções e as mapeia para respostas HTTP com os status codes e corpos de erro apropriados (ex: `InvalidGrantException` → `400 Bad Request`, `ClientNotFoundException` → `404 Not Found`).

**Estrutura Maven:** cada microsserviço é um módulo Maven com `pom.xml` herdando do `pom.xml` pai na raiz do projeto. A estrutura de diretórios segue o padrão Maven: `src/main/java`, `src/main/resources` e `src/test/java`.

---

## Modelos de Dados

O `README.md` não define modelos de dados próprios — ele documenta os contratos de API existentes dos microsserviços. Os modelos de dados relevantes para a documentação são os corpos de requisição/resposta dos endpoints, já descritos nas seções de componentes acima.

Os exemplos de JSON nos blocos `curl` seguem os contratos definidos no design técnico dos microsserviços (`oidc-microservices-ddd-hexagonal/design.md`).

---

## Avaliação de Propriedades de Correção (PBT)

Esta feature produz um artefato de documentação estático (`README.md`). Todos os critérios de aceitação — incluindo os novos requisitos 9 a 12 sobre o "Guia para Novos Integrantes" — são verificações de conteúdo de arquivo de texto: presença de seções, diagramas, exemplos de código, árvores de pacotes e descrições de convenções.

**PBT não se aplica** a esta feature porque:
- Não há lógica de código sendo testada
- Não há funções com input/output variável
- Todos os critérios são verificações de conteúdo estático (classificação SMOKE)
- Não existe "para todo input X, propriedade P(X) vale" — o README é um documento único e fixo

A seção de Correctness Properties é omitida intencionalmente.

---

## Tratamento de Erros

Por se tratar de documentação estática, não há tratamento de erros em runtime. Os pontos de atenção são:

- **Comandos desatualizados:** Se o `docker-compose.yml` for alterado (novas variáveis, novas portas), o README deve ser atualizado manualmente na mesma PR.
- **Exemplos `curl` inválidos:** Os exemplos devem ser testados manualmente contra uma instância local antes de serem publicados.
- **Versões de pré-requisitos:** As versões mínimas de Docker e Java devem ser revisadas a cada release major da plataforma.

---

## Estratégia de Testes

Como todos os critérios de aceitação são do tipo SMOKE (verificação de conteúdo estático), a estratégia de testes é:

**Testes de smoke (verificação manual ou script):**

Cada critério de aceitação pode ser verificado com um script shell simples que inspeciona o conteúdo do `README.md`:

```bash
#!/bin/bash
README="README.md"
PASS=0
FAIL=0

check() {
  local desc="$1"
  local pattern="$2"
  if grep -q "$pattern" "$README"; then
    echo "✓ $desc"
    ((PASS++))
  else
    echo "✗ $desc"
    ((FAIL++))
  fi
}

# Req 1 — Visão Geral
check "Tabela de microsserviços" "api-gateway"
check "Diagrama Mermaid" '```mermaid'
check "Pré-requisitos Docker" "Docker"

# Req 2 — Setup
check "Comando mvn package" "mvn clean package"
check "Comando docker-compose up" "docker-compose up --build"
check "Comando docker-compose ps" "docker-compose ps"
check "Comando docker-compose down" "docker-compose down"
check "Health check actuator" "actuator/health"
check "Migration Tool profile" "--profile migration"

# Req 3 — Endpoints
check "POST /token" "POST /token"
check "POST /register" "POST /register"
check "GET /userinfo" "GET /userinfo"
check "POST /uma/resource_set" "uma/resource_set"
check "GET /scopes" "GET /scopes"

# Req 4 — PKCE
check "code_verifier" "code_verifier"
check "code_challenge" "code_challenge"
check "code_challenge_method=S256" "S256"

# Req 5 — Client Credentials
check "client_credentials" "client_credentials"
check "client_secret_basic" "client_secret_basic"

# Req 6 — UMA
check "uma-ticket grant" "uma-ticket"
check "PermissionTicket" "ticket"

# Req 7 — Env vars
check "SPRING_DATA_MONGODB_URI" "SPRING_DATA_MONGODB_URI"
check "SPRING_KAFKA_BOOTSTRAP_SERVERS" "SPRING_KAFKA_BOOTSTRAP_SERVERS"

# Req 8 — Observabilidade
check "actuator/metrics" "actuator/metrics"
check "traceId" "traceId"
check "Prometheus scrape" "scrape_configs"

# Req 9 — Guia para Novos Integrantes: Arquitetura Hexagonal
check "Seção Guia para Novos Integrantes" "Guia para Novos Integrantes"
check "Três camadas hexagonal" "domain"
check "Camada application" "application"
check "Camada infrastructure" "infrastructure"
check "Domínio sem frameworks" "sem.*framework\|sem dependências\|sem anotações"
check "port/in explicado" "port/in"
check "port/out explicado" "port/out"
check "Diagrama fluxo dependência" "adapter/in"
check "Regra de dependência" "camadas externas\|regra de dependência\|externo depende"

# Req 10 — Organização DDD
check "domain/model/" "domain/model/"
check "domain/model/vo/" "domain/model/vo/"
check "domain/port/in/" "domain/port/in/"
check "domain/port/out/" "domain/port/out/"
check "domain/event/" "domain/event/"
check "domain/exception/" "domain/exception/"
check "application/service/" "application/service/"
check "Exemplo AccessToken" "AccessToken"
check "Exemplo IssueTokenUseCase" "IssueTokenUseCase"
check "Exemplo AccessTokenRepository" "AccessTokenRepository"
check "Exemplo DomainEventPublisher" "DomainEventPublisher"
check "Exemplo AccessTokenIssued" "AccessTokenIssued"
check "Exemplo InvalidGrantException" "InvalidGrantException"

# Req 11 — Estrutura de Pacotes
check "TokenEndpoint" "TokenEndpoint"
check "AuthorizationEndpoint" "AuthorizationEndpoint"
check "IntrospectionEndpoint" "IntrospectionEndpoint"
check "RevocationEndpoint" "RevocationEndpoint"
check "MongoAccessTokenRepository" "MongoAccessTokenRepository"
check "SpringDataAccessTokenRepository" "SpringDataAccessTokenRepository"
check "AccessTokenDocument" "AccessTokenDocument"
check "KafkaDomainEventPublisher" "KafkaDomainEventPublisher"
check "ClientRegistryRestAdapter" "ClientRegistryRestAdapter"
check "ScopeManagerRestAdapter" "ScopeManagerRestAdapter"
check "MongoConfig" "MongoConfig"
check "KafkaConfig" "KafkaConfig"
check "SecurityConfig" "SecurityConfig"
check "JwkConfig" "JwkConfig"

# Req 12 — Convenções Arquiteturais
check "Isolamento de dados MongoDB" "banco de dados.*dedicado\|isolamento de dados\|próprio.*MongoDB"
check "Comunicação assíncrona Kafka" "assíncrona.*Kafka\|Kafka.*assíncrona"
check "Comunicação síncrona REST" "síncrona.*REST\|REST.*síncrona"
check "Entidades são POJOs" "POJO\|sem anotações de framework"
check "Value Objects imutáveis" "imutáveis\|imutável"
check "DomainException" "DomainException"
check "GlobalExceptionHandler" "GlobalExceptionHandler"
check "Estrutura Maven" "pom.xml\|Maven"

echo ""
echo "Resultado: ${PASS} passou, ${FAIL} falhou"
[ $FAIL -eq 0 ] && exit 0 || exit 1
```

Este script pode ser executado como parte de um CI pipeline para garantir que o README não perde seções críticas em futuras edições.

**Testes de integração (manuais):**
- Executar cada exemplo `curl` contra uma instância local da plataforma e verificar que as respostas correspondem ao documentado
- Seguir o fluxo PKCE completo do início ao fim usando os comandos do README
- Seguir o fluxo UMA completo do início ao fim
