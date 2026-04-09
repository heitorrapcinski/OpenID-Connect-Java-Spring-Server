# Plano de Implementação: Documentação da Plataforma (README.md)

## Visão Geral

Criar o arquivo `README.md` na raiz do projeto com documentação completa da plataforma OIDC/OAuth2/UMA. O output é um único arquivo Markdown estático, construído seção por seção conforme o design.

## Tarefas

- [ ] 1. Criar o README.md com cabeçalho e seção de Visão Geral da Plataforma
  - Criar o arquivo `README.md` na raiz do projeto
  - Escrever a introdução descrevendo a plataforma como implementação de referência de microsserviços OIDC/OAuth2/UMA com DDD e arquitetura hexagonal (Spring Boot 3, MongoDB, Kafka)
  - Adicionar a tabela de microsserviços com nome, porta e responsabilidade para todos os 7 serviços (api-gateway, authorization-server, client-registry, oidc-provider, uma-server, scope-manager, migration-tool)
  - Adicionar o diagrama de arquitetura em Mermaid (`graph TB`) mostrando cliente externo → API Gateway → microsserviços → MongoDB (5 instâncias) e Kafka, com ordem de dependência de startup
  - Adicionar a lista de pré-requisitos com versões mínimas: Docker 24+, Docker Compose v2.20+, Java 21 + Maven 3.9
  - _Requisitos: 1.1, 1.2, 1.3, 1.4_

- [ ] 2. Adicionar seção "Como Subir a Plataforma"
  - Escrever os passos sequenciais numerados: clone → `mvn clean package -DskipTests` → `docker-compose up --build -d`
  - Documentar comandos de operação: `docker-compose ps`, `docker-compose down`, `docker-compose up --build -d <serviço>`
  - Documentar o comando da Migration Tool com profile separado: `docker-compose --profile migration up migration-tool`
  - Adicionar os comandos `curl` de health check para cada microsserviço (`GET http://localhost:{porta}/actuator/health`)
  - Adicionar tabela de portas de infraestrutura: MongoDB (27017–27021) e Kafka (9092)
  - _Requisitos: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9_

- [ ] 3. Adicionar seção de Referência de Endpoints
  - [ ] 3.1 Documentar endpoints do Authorization Server
    - `GET/POST /authorize`, `POST /token` com exemplos para `authorization_code` (PKCE), `client_credentials` (Basic auth) e `refresh_token`, `POST /revoke`, `POST /introspect`
    - Incluir header `Authorization: Bearer` ou `-u client_id:client_secret` nos exemplos que exigem autenticação
    - _Requisitos: 3.1, 3.2, 3.7, 3.8_

  - [ ] 3.2 Documentar endpoints do Client Registry
    - `POST /register` com corpo de requisição e resposta 201 (client_id, client_secret, registration_access_token)
    - `GET /register/{client_id}`, `PUT /register/{client_id}`, `DELETE /register/{client_id}`
    - _Requisitos: 3.1, 3.3, 3.7, 3.8_

  - [ ] 3.3 Documentar endpoints do OIDC Provider, UMA Server e Scope Manager
    - OIDC Provider: `GET /userinfo`, `GET /.well-known/openid-configuration`, `GET /jwks` com exemplo de discovery document
    - UMA Server: `POST /uma/resource_set`, `GET /uma/resource_set/{id}`, `PUT /uma/resource_set/{id}`, `DELETE /uma/resource_set/{id}`, `POST /uma/permission`
    - Scope Manager: `GET /scopes`, `GET /scopes/defaults`, `POST /scopes`, `PUT /scopes/{value}`, `DELETE /scopes/{value}`
    - _Requisitos: 3.1, 3.4, 3.5, 3.6, 3.7, 3.8_

- [ ] 4. Adicionar seção de Integração — Authorization Code + PKCE
  - Passo 1: `POST /register` com `grant_types: ["authorization_code", "refresh_token"]` e `token_endpoint_auth_method: "none"` (cliente público/SPA)
  - Passo 2: Geração de `code_verifier` e `code_challenge` (SHA-256 + Base64URL) com exemplo em JavaScript usando Web Crypto API
  - Passo 3: Construção da URL de autorização com todos os parâmetros (`response_type`, `client_id`, `redirect_uri`, `scope`, `state`, `code_challenge`, `code_challenge_method=S256`)
  - Passo 4: Troca do `authorization_code` pelo token via `POST /token` com `code_verifier` — exemplo `curl` completo
  - Passo 5: Chamada ao `GET /userinfo` com `Authorization: Bearer {access_token}`
  - Passo 6: Renovação via `POST /token` com `grant_type=refresh_token`
  - _Requisitos: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7_

- [ ] 5. Adicionar seção de Integração — Client Credentials
  - Passo 1: `POST /register` com `grant_types: ["client_credentials"]` e `token_endpoint_auth_method: "client_secret_basic"`
  - Passo 2: `POST /token` com `grant_type=client_credentials`, autenticação Basic (`-u client_id:client_secret`) e parâmetro `scope`
  - Passo 3: Uso do token em `POST /introspect` com autenticação Basic
  - _Requisitos: 5.1, 5.2, 5.3, 5.4_

- [ ] 6. Adicionar seção de Integração — UMA 2.0
  - Descrever os 4 papéis: Resource Owner, Resource Server, Requesting Party, Authorization Server
  - Passo 1: Registro de ResourceSet via `POST /uma/resource_set` com exemplo de corpo e resposta
  - Passo 2: Criação de PermissionTicket via `POST /uma/permission` (Resource Server ao receber requisição sem token)
  - Passo 3: Obtenção do RPT via `POST /token` com `grant_type=urn:ietf:params:oauth:grant-type:uma-ticket` e o ticket
  - Passo 4: Validação do RPT via `POST /introspect` pelo Resource Server
  - _Requisitos: 6.1, 6.2, 6.3, 6.4, 6.5_

- [ ] 7. Adicionar seção de Variáveis de Ambiente
  - Tabela do Authorization Server: `SPRING_DATA_MONGODB_URI`, `SPRING_KAFKA_BOOTSTRAP_SERVERS`, `SERVER_PORT`, `SCOPE_MANAGER_URL`, `CLIENT_REGISTRY_URL` — com valores padrão do `docker-compose.yml`
  - Tabela do Client Registry: `SPRING_DATA_MONGODB_URI`, `SPRING_KAFKA_BOOTSTRAP_SERVERS`, `SERVER_PORT`, `SCOPE_MANAGER_URL`
  - Tabela do OIDC Provider: `SPRING_DATA_MONGODB_URI`, `SPRING_KAFKA_BOOTSTRAP_SERVERS`, `SERVER_PORT`, `AUTHORIZATION_SERVER_URL`
  - Tabela do UMA Server: `SPRING_DATA_MONGODB_URI`, `SPRING_KAFKA_BOOTSTRAP_SERVERS`, `SERVER_PORT`
  - Tabela do Scope Manager: `SPRING_DATA_MONGODB_URI`, `SERVER_PORT`
  - Tabela do API Gateway: `SERVER_PORT`, `AUTHORIZATION_SERVER_URL`, `CLIENT_REGISTRY_URL`, `OIDC_PROVIDER_URL`, `UMA_SERVER_URL`, `SCOPE_MANAGER_URL`
  - Tabela da Migration Tool: `MONGODB_AUTH_URI`, `MONGODB_CLIENT_URI`, `MONGODB_OIDC_URI`, `MONGODB_UMA_URI`, `MONGODB_SCOPE_URI`
  - _Requisitos: 7.1, 7.2, 7.3, 7.4_

- [ ] 8. Adicionar seção de Observabilidade
  - Tabela de endpoints Actuator disponíveis em todos os microsserviços: `/actuator/health`, `/actuator/metrics`, `/actuator/info`
  - Exemplo de bloco JSON do formato de log estruturado com campos: `timestamp`, `level`, `service`, `traceId`, `spanId`, `message`
  - Exemplo de configuração de scrape Prometheus (`scrape_configs`) com todos os 6 microsserviços
  - Script shell de health check que itera sobre todos os serviços e exibe o HTTP status de cada `/actuator/health`
  - _Requisitos: 8.1, 8.2, 8.3, 8.4_

- [ ] 9. Checkpoint final — Verificar completude do README
  - Executar o script de smoke test do design (`grep`-based) para confirmar que todas as seções e padrões obrigatórios estão presentes no `README.md`
  - Garantir que todos os blocos de código estão corretamente fechados e que o Markdown está bem formado
  - Perguntar ao usuário se há ajustes antes de finalizar.

## Notas

- Não há geração automática de código — o output desta spec é o próprio arquivo `README.md`
- Todos os valores padrão das variáveis de ambiente devem ser extraídos do `docker-compose.yml` existente
- Os exemplos `curl` usam `http://localhost:8090` como base URL (API Gateway)
- PBT não se aplica a esta feature (artefato estático, sem lógica de código)
