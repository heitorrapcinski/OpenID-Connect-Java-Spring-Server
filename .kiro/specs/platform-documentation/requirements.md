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
- **Developer_Onboarding**: Novo integrante do time de desenvolvimento que está se familiarizando com a plataforma e sua arquitetura.
- **Hexagonal_Architecture**: Padrão arquitetural (Ports & Adapters) que isola o núcleo de negócio (`domain`) de tecnologias externas via interfaces (`ports`) e implementações (`adapters`).
- **DDD**: Domain-Driven Design — abordagem de design de software que organiza o código em torno do domínio de negócio, com entidades, value objects, eventos e exceções de domínio.
- **Port_In**: Interface de caso de uso definida no `domain/port/in/`, implementada pela camada `application/service/`.
- **Port_Out**: Interface de repositório ou publisher definida no `domain/port/out/`, implementada pela camada `infrastructure/adapter/out/`.
- **Value_Object**: Objeto imutável do domínio que encapsula um conceito com semântica e validação próprias, localizado em `domain/model/vo/`.
- **Domain_Event**: Evento publicado pelo domínio após uma operação relevante, localizado em `domain/event/` e publicado via Kafka pelo adaptador em `infrastructure/adapter/out/messaging/`.
- **Domain_Exception**: Exceção tipada que representa uma violação de regra de negócio, localizada em `domain/exception/` e estendendo `DomainException`.

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

---

### Requisito 9: Seção "Guia para Novos Integrantes" — Visão Geral da Arquitetura Hexagonal

**User Story:** Como Developer_Onboarding, quero entender a arquitetura hexagonal adotada na plataforma, para que eu saiba como o código está organizado e quais são as regras de dependência entre as camadas antes de começar a contribuir.

#### Critérios de Aceitação

1. THE README SHALL conter uma seção "Guia para Novos Integrantes" com subseção dedicada à arquitetura hexagonal (Ports & Adapters).
2. THE README SHALL descrever as três camadas da arquitetura hexagonal adotada: `domain` (núcleo de negócio, sem dependências externas), `application` (orquestração de casos de uso) e `infrastructure` (adaptadores de entrada e saída).
3. THE README SHALL explicar que a camada `domain` não possui dependências de frameworks (Spring, MongoDB, Kafka) e contém apenas lógica de negócio pura.
4. THE README SHALL explicar que os `port/in` são interfaces de casos de uso definidas no domínio e implementadas na camada `application`, e que os `port/out` são interfaces de repositórios e publishers definidas no domínio e implementadas na camada `infrastructure`.
5. THE README SHALL conter um diagrama (Mermaid ou ASCII) ilustrando o fluxo de dependência: `infrastructure/adapter/in` → `application/service` → `domain/port/in` e `domain/port/out` ← `infrastructure/adapter/out`.
6. THE README SHALL explicar a regra de dependência: camadas externas dependem de camadas internas, nunca o contrário, e o domínio não conhece nenhuma tecnologia de infraestrutura.

---

### Requisito 10: Seção "Guia para Novos Integrantes" — Organização DDD de Cada Microsserviço

**User Story:** Como Developer_Onboarding, quero entender como o DDD é aplicado dentro de cada microsserviço, para que eu saiba onde encontrar entidades, value objects, eventos de domínio e exceções ao navegar pelo código.

#### Critérios de Aceitação

1. THE README SHALL conter uma subseção descrevendo a organização DDD dentro de cada microsserviço, com os subpacotes de `domain/` e seus propósitos.
2. THE README SHALL documentar que `domain/model/` contém as entidades e agregados do domínio (ex: `AccessToken`, `RefreshToken`, `AuthorizationCode` no `authorization-server`).
3. THE README SHALL documentar que `domain/model/vo/` contém os Value Objects imutáveis que encapsulam conceitos do domínio (ex: `ClientId`, `TokenValue`, `PKCEChallenge`, `Scope`).
4. THE README SHALL documentar que `domain/port/in/` contém as interfaces de casos de uso (ex: `IssueTokenUseCase`, `RevokeTokenUseCase`, `IntrospectTokenUseCase`) e que cada interface representa uma intenção de negócio.
5. THE README SHALL documentar que `domain/port/out/` contém as interfaces de saída do domínio (ex: `AccessTokenRepository`, `DomainEventPublisher`, `ClientQueryPort`, `ScopeQueryPort`) que abstraem persistência e comunicação externa.
6. THE README SHALL documentar que `domain/event/` contém os eventos de domínio publicados após operações relevantes (ex: `AccessTokenIssued`, `AccessTokenRevoked`, `RefreshTokenIssued`).
7. THE README SHALL documentar que `domain/exception/` contém as exceções de domínio tipadas que representam violações de regras de negócio (ex: `InvalidGrantException`, `ClientNotFoundException`, `InvalidScopeException`).
8. THE README SHALL documentar que `application/service/` contém as implementações dos casos de uso definidos em `domain/port/in/`, orquestrando chamadas ao domínio e às portas de saída.

---

### Requisito 11: Seção "Guia para Novos Integrantes" — Estrutura de Pacotes Padrão com Exemplos Reais

**User Story:** Como Developer_Onboarding, quero ver a estrutura de pacotes padrão de um microsserviço com exemplos reais de arquivos do código, para que eu possa navegar no projeto com confiança e entender onde criar novos artefatos.

#### Critérios de Aceitação

1. THE README SHALL conter uma subseção com a árvore de pacotes padrão completa de um microsserviço, usando o `authorization-server` como exemplo canônico.
2. THE README SHALL mostrar a estrutura de pacotes `infrastructure/adapter/in/web/` com exemplos reais de controllers REST (ex: `TokenEndpoint`, `AuthorizationEndpoint`, `IntrospectionEndpoint`, `RevocationEndpoint`).
3. THE README SHALL mostrar a estrutura de pacotes `infrastructure/adapter/out/persistence/` com exemplos reais de repositórios MongoDB (ex: `MongoAccessTokenRepository`, `MongoRefreshTokenRepository`) e documentos de persistência em `persistence/document/` (ex: `AccessTokenDocument`, `RefreshTokenDocument`).
4. THE README SHALL mostrar a estrutura de pacotes `infrastructure/adapter/out/messaging/` com exemplos reais de publishers Kafka (ex: `KafkaDomainEventPublisher`).
5. THE README SHALL mostrar a estrutura de pacotes `infrastructure/adapter/out/rest/` com exemplos reais de clientes HTTP para outros microsserviços (ex: `ClientRegistryRestAdapter`, `ScopeManagerRestAdapter`).
6. THE README SHALL mostrar a estrutura de pacotes `infrastructure/config/` com exemplos reais de classes de configuração (ex: `MongoConfig`, `KafkaConfig`, `SecurityConfig`, `JwkConfig`).
7. THE README SHALL documentar que o padrão de nomenclatura dos adaptadores de persistência segue `Mongo{Entidade}Repository` para a implementação e `SpringData{Entidade}Repository` para a interface Spring Data subjacente.
8. WHEN um novo microsserviço for criado, THE README SHALL indicar que o Developer_Onboarding deve replicar a mesma estrutura de pacotes `domain/`, `application/` e `infrastructure/` com os mesmos subpacotes.

---

### Requisito 12: Seção "Guia para Novos Integrantes" — Convenções e Decisões Arquiteturais

**User Story:** Como Developer_Onboarding, quero conhecer as convenções e decisões arquiteturais importantes adotadas na plataforma, para que eu possa contribuir com código consistente e evitar violações das regras estabelecidas.

#### Critérios de Aceitação

1. THE README SHALL conter uma subseção listando as convenções e decisões arquiteturais importantes adotadas em todos os microsserviços.
2. THE README SHALL documentar que cada microsserviço possui seu próprio banco de dados MongoDB dedicado e que nenhum microsserviço acessa diretamente o banco de dados de outro (isolamento de dados por serviço).
3. THE README SHALL documentar que a comunicação assíncrona entre microsserviços é feita exclusivamente via eventos de domínio publicados no Kafka, e que os eventos são definidos em `domain/event/`.
4. THE README SHALL documentar que a comunicação síncrona entre microsserviços (ex: `authorization-server` consultando `client-registry` e `scope-manager`) é feita via adaptadores REST em `infrastructure/adapter/out/rest/`, implementando interfaces de porta de saída definidas no domínio.
5. THE README SHALL documentar que as entidades de domínio em `domain/model/` são POJOs sem anotações de framework (sem `@Document`, `@Entity`, `@JsonProperty`), e que o mapeamento para documentos MongoDB é feito nas classes `*Document` em `infrastructure/adapter/out/persistence/document/`.
6. THE README SHALL documentar que Value Objects em `domain/model/vo/` são imutáveis (campos `final`, sem setters) e encapsulam validação e semântica do conceito que representam.
7. THE README SHALL documentar que exceções de domínio em `domain/exception/` estendem uma classe base `DomainException` e são mapeadas para respostas HTTP pelo `GlobalExceptionHandler` em `infrastructure/adapter/in/web/`.
8. THE README SHALL documentar a estrutura do módulo Maven de cada microsserviço: módulo raiz com `pom.xml` herdando do `pom.xml` pai na raiz do projeto, com `src/main/java`, `src/main/resources` e `src/test/java`.
