# Documento de Requisitos

## Introdução

Este documento descreve os requisitos para a criação da documentação completa da plataforma de microsserviços OIDC/OAuth2/UMA no arquivo `README.md` do projeto. A documentação deve cobrir três áreas principais: como subir a plataforma localmente via Docker Compose, como usar os serviços (endpoints principais com exemplos), e como conectar uma webapp para interagir com os fluxos OAuth2/OIDC.

A plataforma é composta pelos microsserviços `api-gateway` (8090), `authorization-server` (8080), `client-registry` (8081), `oidc-provider` (8082), `uma-server` (8083) e `scope-manager` (8084), com infraestrutura de 5 instâncias MongoDB e Kafka em modo KRaft, orquestrados via `docker-compose.yml`.

---

## Glossário

- **README**: Arquivo `README.md` na raiz do projeto, ponto de entrada da documentação.
- **Platform**: O conjunto completo de microsserviços, bancos de dados e infraestrutura de mensageria da plataforma OIDC/OAuth2/UMA.
- **Developer**: Desenvolvedor que deseja subir, usar ou integrar a plataforma.
- **Webapp_Developer**: Desenvolvedor que deseja conectar uma aplicação web à plataforma usando fluxos OAuth2/OIDC.
- **Operator**: Pessoa responsável por operar e monitorar a plataforma em execução.
- **Authorization_Server**: Microsserviço na porta 8080 responsável pelos fluxos OAuth 2.0.
- **Client_Registry**: Microsserviço na porta 8081 responsável pelo registro dinâmico de clientes.
- **OIDC_Provider**: Microsserviço na porta 8082 responsável pelos fluxos OpenID Connect.
- **UMA_Server**: Microsserviço na porta 8083 responsável pelo protocolo UMA 2.0.
- **Scope_Manager**: Microsserviço na porta 8084 responsável pelo gerenciamento de escopos.
- **API_Gateway**: Microsserviço na porta 8090 que roteia requisições públicas para os microsserviços internos.
- **Migration_Tool**: Ferramenta run-once para migração de dados de bancos relacionais para MongoDB.
- **Docker_Compose**: Ferramenta de orquestração de containers usada para subir a plataforma localmente.
- **OAuth2_Flow**: Fluxo de autorização definido pelo protocolo OAuth 2.0 (Authorization Code, Client Credentials, etc.).
- **OIDC_Flow**: Fluxo de autenticação definido pelo protocolo OpenID Connect 1.0.
- **UMA_Flow**: Fluxo de autorização delegada definido pelo protocolo UMA 2.0.
- **Access_Token**: Token JWT emitido pelo Authorization_Server para autorizar acesso a recursos.
- **ID_Token**: Token JWT emitido pelo Authorization_Server contendo claims de identidade do usuário.
- **Registration_Access_Token**: Token emitido pelo Client_Registry para gerenciar o registro de um cliente.
- **PKCE**: Proof Key for Code Exchange (RFC 7636), extensão de segurança para o fluxo Authorization Code.

---

## Requisitos

### Requisito 1: Seção de Visão Geral da Plataforma

**User Story:** Como Developer, quero uma visão geral da plataforma no README, para que eu entenda rapidamente o que é o sistema, quais serviços o compõem e como eles se relacionam.

#### Critérios de Aceitação

1. THE README SHALL conter uma seção de introdução descrevendo o propósito da plataforma como uma implementação de microsserviços OIDC/OAuth2/UMA baseada em DDD e arquitetura hexagonal.
2. THE README SHALL conter uma tabela listando todos os microsserviços com seus nomes, portas expostas e responsabilidades.
3. THE README SHALL conter um diagrama de arquitetura (Mermaid ou ASCII) mostrando os microsserviços, o API_Gateway, as instâncias MongoDB e o Kafka, com as conexões entre eles.
4. THE README SHALL listar os pré-requisitos de software necessários para subir a plataforma, incluindo versões mínimas de Docker, Docker Compose e Java.

---

### Requisito 2: Seção de Como Subir a Plataforma

**User Story:** Como Developer, quero instruções claras para subir a plataforma localmente, para que eu possa ter o ambiente funcionando sem precisar consultar outros documentos.

#### Critérios de Aceitação

1. THE README SHALL conter uma seção "Como Subir a Plataforma" com os passos sequenciais para clonar o repositório, compilar os serviços e iniciar os containers.
2. WHEN o Developer seguir os passos documentados, THE README SHALL instruir o Developer a executar `mvn clean package -DskipTests` antes de `docker-compose up --build` para garantir que os JARs estejam disponíveis para os Dockerfiles.
3. THE README SHALL documentar o comando para subir todos os serviços: `docker-compose up --build -d`.
4. THE README SHALL documentar o comando para verificar o status dos containers: `docker-compose ps`.
5. THE README SHALL documentar os endpoints de health check de cada microsserviço no formato `GET http://localhost:{porta}/actuator/health`.
6. THE README SHALL documentar o comando para parar e remover os containers: `docker-compose down`.
7. THE README SHALL documentar o comando para subir a Migration_Tool separadamente usando o profile Docker Compose: `docker-compose --profile migration up migration-tool`.
8. THE README SHALL documentar as portas expostas de cada serviço de infraestrutura: MongoDB instâncias (27017–27021) e Kafka (9092).
9. IF o Developer precisar reconstruir apenas um serviço específico, THE README SHALL documentar o comando `docker-compose up --build -d {nome-do-serviço}`.

---

### Requisito 3: Seção de Endpoints e Exemplos de Uso

**User Story:** Como Developer, quero exemplos práticos de uso dos endpoints principais de cada microsserviço, para que eu possa testar e integrar a plataforma rapidamente.

#### Critérios de Aceitação

1. THE README SHALL conter uma seção de referência de endpoints para cada microsserviço, com método HTTP, caminho, descrição e exemplo de requisição/resposta em formato `curl`.
2. THE README SHALL documentar os endpoints do Authorization_Server: `GET/POST /authorize`, `POST /token` (com exemplos para `authorization_code`, `client_credentials` e `refresh_token`), `POST /revoke` e `POST /introspect`.
3. THE README SHALL documentar os endpoints do Client_Registry: `POST /register`, `GET /register/{client_id}`, `PUT /register/{client_id}` e `DELETE /register/{client_id}`, com exemplos de corpo de requisição e resposta.
4. THE README SHALL documentar os endpoints do OIDC_Provider: `GET /userinfo`, `GET /.well-known/openid-configuration` e `GET /jwks`.
5. THE README SHALL documentar os endpoints do UMA_Server: `POST /uma/resource_set`, `GET /uma/resource_set/{id}`, `PUT /uma/resource_set/{id}`, `DELETE /uma/resource_set/{id}` e `POST /uma/permission`.
6. THE README SHALL documentar os endpoints do Scope_Manager: `GET /scopes`, `GET /scopes/defaults`, `POST /scopes`, `PUT /scopes/{value}` e `DELETE /scopes/{value}`.
7. THE README SHALL documentar que todos os endpoints públicos são acessíveis via API_Gateway na porta 8090, com exemplos usando `http://localhost:8090` como base URL.
8. WHEN um endpoint exigir autenticação, THE README SHALL incluir no exemplo `curl` o header `Authorization: Bearer {access_token}` ou as credenciais de cliente via `-u client_id:client_secret`.

---

### Requisito 4: Seção de Integração de Webapp — Fluxo Authorization Code com PKCE

**User Story:** Como Webapp_Developer, quero um guia passo a passo do fluxo Authorization Code com PKCE, para que eu possa implementar autenticação segura na minha aplicação web sem armazenar segredos de cliente no frontend.

#### Critérios de Aceitação

1. THE README SHALL conter uma seção dedicada ao fluxo Authorization Code com PKCE, descrevendo cada etapa com o endpoint correspondente, parâmetros necessários e exemplo de resposta.
2. THE README SHALL documentar o passo de registro do cliente via `POST /register` com `grant_types: ["authorization_code", "refresh_token"]` e `token_endpoint_auth_method: "none"` para clientes públicos (SPA/mobile).
3. THE README SHALL documentar o passo de geração do `code_verifier` e `code_challenge` (SHA-256, Base64URL) com exemplo de código em JavaScript.
4. THE README SHALL documentar a construção da URL de autorização com os parâmetros `response_type=code`, `client_id`, `redirect_uri`, `scope`, `state`, `code_challenge` e `code_challenge_method=S256`.
5. THE README SHALL documentar o passo de troca do `authorization_code` pelo Access_Token via `POST /token` com o `code_verifier`, incluindo exemplo `curl` completo.
6. THE README SHALL documentar como usar o Access_Token para chamar o endpoint `GET /userinfo` e obter o ID_Token.
7. THE README SHALL documentar o passo de renovação do Access_Token via `POST /token` com `grant_type=refresh_token`.

---

### Requisito 5: Seção de Integração de Webapp — Fluxo Client Credentials

**User Story:** Como Webapp_Developer, quero um guia do fluxo Client Credentials, para que eu possa autenticar serviços backend-to-backend sem interação do usuário.

#### Critérios de Aceitação

1. THE README SHALL conter uma seção dedicada ao fluxo Client Credentials, descrevendo cada etapa com exemplos `curl`.
2. THE README SHALL documentar o registro do cliente com `grant_types: ["client_credentials"]` e `token_endpoint_auth_method: "client_secret_basic"`.
3. THE README SHALL documentar a obtenção do Access_Token via `POST /token` com `grant_type=client_credentials`, autenticação Basic e parâmetro `scope`.
4. THE README SHALL documentar como usar o Access_Token resultante para chamar APIs protegidas, com exemplo de chamada ao endpoint `POST /introspect`.

---

### Requisito 6: Seção de Integração de Webapp — Fluxo UMA 2.0

**User Story:** Como Webapp_Developer, quero um guia do fluxo UMA 2.0, para que eu possa implementar autorização delegada onde o Resource Owner controla o acesso aos seus recursos.

#### Critérios de Aceitação

1. THE README SHALL conter uma seção dedicada ao fluxo UMA 2.0, descrevendo os papéis (Resource Owner, Resource Server, Requesting Party, Authorization Server) e as etapas do fluxo.
2. THE README SHALL documentar o registro de um ResourceSet via `POST /uma/resource_set` com exemplo de corpo de requisição e resposta.
3. THE README SHALL documentar a criação de um PermissionTicket via `POST /uma/permission` quando o Resource Server receber uma requisição sem token adequado.
4. THE README SHALL documentar a obtenção do Requesting Party Token (RPT) via `POST /token` com `grant_type=urn:ietf:params:oauth:grant-type:uma-ticket` e o ticket obtido.
5. THE README SHALL documentar como o Resource Server valida o RPT via `POST /introspect` antes de conceder acesso ao recurso.

---

### Requisito 7: Seção de Variáveis de Ambiente e Configuração

**User Story:** Como Operator, quero uma referência completa das variáveis de ambiente de cada microsserviço, para que eu possa configurar a plataforma para diferentes ambientes (desenvolvimento, staging, produção).

#### Critérios de Aceitação

1. THE README SHALL conter uma tabela de variáveis de ambiente para cada microsserviço, com nome da variável, descrição, valor padrão (conforme `docker-compose.yml`) e se é obrigatória.
2. THE README SHALL documentar as variáveis de ambiente do Authorization_Server: `SPRING_DATA_MONGODB_URI`, `SPRING_KAFKA_BOOTSTRAP_SERVERS`, `SERVER_PORT`, `SCOPE_MANAGER_URL` e `CLIENT_REGISTRY_URL`.
3. THE README SHALL documentar as variáveis de ambiente do API_Gateway: `SERVER_PORT`, `AUTHORIZATION_SERVER_URL`, `CLIENT_REGISTRY_URL`, `OIDC_PROVIDER_URL`, `UMA_SERVER_URL` e `SCOPE_MANAGER_URL`.
4. THE README SHALL documentar as variáveis de ambiente da Migration_Tool: `MONGODB_AUTH_URI`, `MONGODB_CLIENT_URI`, `MONGODB_OIDC_URI`, `MONGODB_UMA_URI` e `MONGODB_SCOPE_URI`.

---

### Requisito 8: Seção de Observabilidade

**User Story:** Como Operator, quero saber como monitorar a saúde e as métricas da plataforma, para que eu possa detectar e diagnosticar problemas em produção.

#### Critérios de Aceitação

1. THE README SHALL documentar os endpoints de observabilidade disponíveis em cada microsserviço: `/actuator/health`, `/actuator/metrics` e `/actuator/info`.
2. THE README SHALL documentar o formato dos logs estruturados em JSON emitidos pelos microsserviços, com os campos `timestamp`, `level`, `service`, `traceId`, `spanId` e `message`.
3. THE README SHALL documentar que as métricas estão disponíveis no formato Prometheus e fornecer um exemplo de configuração de scrape para o Prometheus.
4. WHEN o Developer quiser verificar a saúde de todos os serviços de uma vez, THE README SHALL documentar um comando `curl` ou script shell que consulte o `/actuator/health` de cada microsserviço sequencialmente.
