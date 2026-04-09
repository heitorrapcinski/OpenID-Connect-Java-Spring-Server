# Plano de Implementação: OIDC Microservices DDD Hexagonal

## Visão Geral

Refatoração do MITREid Connect monolítico em cinco microserviços independentes (scope-manager, client-registry, authorization-server, oidc-provider, uma-server) seguindo DDD e arquitetura hexagonal, com persistência MongoDB, comunicação via REST e Kafka, e ferramentas de migração e observabilidade.

A ordem de implementação respeita as dependências entre serviços: scope-manager → client-registry → authorization-server → oidc-provider → uma-server.

## Tarefas

- [x] 1. Preservar código legado e preparar workspace
  - Mover todo o conteúdo da pasta raiz do workspace (exceto a pasta `.kiro`) para uma nova pasta `legacy/`
  - Isso preserva o código original do monolito como referência durante a implementação da nova arquitetura
  - Arquivos a mover: todos os diretórios e arquivos na raiz (openid-connect-server, openid-connect-client, openid-connect-common, etc.), exceto `.kiro/`
  - _Requirements: 1.1_

- [x] 2. Criar estrutura Maven multi-módulo raiz
  - Criar `pom.xml` raiz com `<packaging>pom</packaging>` e módulos: `scope-manager`, `client-registry`, `authorization-server`, `oidc-provider`, `uma-server`, `api-gateway`, `migration-tool`
  - Definir `<dependencyManagement>` com Spring Boot 3.3.0 BOM, Spring Cloud BOM, Nimbus JOSE+JWT 9.37.3, jqwik 1.8.4, Testcontainers BOM
  - Definir propriedades globais: `java.version=21`, `maven.compiler.source=21`, `maven.compiler.target=21`
  - _Requirements: 1.1, 2.1_


- [x] 3. Implementar microserviço `scope-manager` (sem dependências externas)
  - [x] 3.1 Criar estrutura de pacotes hexagonal e domínio do scope-manager
    - Criar `scope-manager/pom.xml` com dependências: spring-boot-starter-web, spring-boot-starter-data-mongodb, spring-boot-starter-security, spring-boot-starter-actuator, nimbus-jose-jwt
    - Criar `SystemScope` Aggregate Root com Value Object `ScopeValue` (imutável, validação de não-nulo/não-vazio)
    - Criar exceções de domínio: `ScopeAlreadyExistsException`, `ScopeNotFoundException`
    - Criar Driving Ports: `CreateScopeUseCase`, `UpdateScopeUseCase`, `DeleteScopeUseCase`, `ListScopesUseCase`
    - Criar Driven Port: `SystemScopeRepository` (findAll, findByValue, findAllDefault, save, deleteByValue)
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 3.11, 10.1, 10.2_

  - [ ]* 3.2 Escrever testes unitários para o domínio do scope-manager
    - Testar invariantes de `SystemScope`: `ScopeValue` não pode ser nulo/vazio
    - Testar que operações inválidas lançam exceções de domínio tipadas
    - _Requirements: 3.12_

  - [x] 3.3 Implementar Application Services do scope-manager
    - Implementar `ScopeApplicationService` implementando todos os Use Case ports
    - Injetar `SystemScopeRepository` via construtor (sem acoplamento a MongoDB)
    - _Requirements: 2.5, 10.1, 10.2_

  - [x] 3.4 Implementar adapter de persistência MongoDB do scope-manager
    - Criar `MongoSystemScopeRepository` implementando `SystemScopeRepository`
    - Criar documento `SystemScopeDocument` com `@Document("system_scopes")`, campo `_id` = `value`
    - Criar índice único em `value` via `@Indexed(unique=true)`
    - Criar `MongoConfig` com configuração de conexão via `spring.data.mongodb.uri`
    - _Requirements: 5.1, 5.2, 5.5_

  - [ ]* 3.5 Escrever teste de integração MongoDB para scope-manager
    - Usar Testcontainers (`MongoDBContainer`) para testar `MongoSystemScopeRepository`
    - Verificar criação de índice único em `value`
    - Testar operações CRUD e idempotência de upsert
    - _Requirements: 5.1, 5.5_

  - [x] 3.6 Implementar REST adapter do scope-manager
    - Criar `ScopeController` com endpoints: `GET /scopes`, `GET /scopes/defaults`, `POST /scopes`, `PUT /scopes/{value}`, `DELETE /scopes/{value}`
    - Criar `GlobalExceptionHandler` (`@ControllerAdvice`) mapeando exceções de domínio para HTTP
    - Criar `SecurityConfig` protegendo `POST/PUT/DELETE /scopes` com `ROLE_ADMIN`; `GET /scopes*` público
    - Criar `application.yml` com porta 8084, nome do serviço, URI MongoDB
    - Criar classe `ScopeManagerApplication` com `@SpringBootApplication`
    - _Requirements: 1.4, 2.2, 10.1, 10.2, 10.3, 10.4_

  - [x] 3.7 Checkpoint — scope-manager
    - Garantir que todos os testes do scope-manager passam. Verificar que `GET /scopes` e `GET /scopes/defaults` retornam respostas corretas. Perguntar ao usuário se há dúvidas antes de continuar.

- [x] 4. Implementar microserviço `client-registry`
  - [x] 4.1 Criar estrutura de pacotes hexagonal e domínio do client-registry
    - Criar `client-registry/pom.xml` com dependências: spring-boot-starter-web, spring-boot-starter-data-mongodb, spring-boot-starter-security, spring-boot-starter-actuator, spring-kafka, nimbus-jose-jwt
    - Criar `Client` Aggregate Root com Value Objects: `ClientId`, `ClientSecret`, `RedirectUri`, `GrantType`, `ResponseType`, `JwsAlgorithm`, `JweAlgorithm`, `SectorIdentifierUri`
    - Implementar invariantes: `grant_types` e `response_types` compatíveis; `redirect_uris` HTTPS para clientes web; `idTokenValiditySeconds` não nulo (default 600)
    - Criar Domain Events: `ClientRegistered`, `ClientUpdated`, `ClientDeleted` (com envelope padrão: eventId, eventType, aggregateId, occurredAt, serviceOrigin, traceId, payload)
    - Criar exceções de domínio: `ClientNotFoundException`, `InvalidClientMetadataException`, `ClientAlreadyExistsException`
    - Criar Driving Ports: `RegisterClientUseCase`, `GetClientUseCase`, `UpdateClientUseCase`, `DeleteClientUseCase`
    - Criar Driven Ports: `ClientRepository`, `ScopeQueryPort` (consulta scope-manager), `DomainEventPublisher`
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 3.1, 4.5, 4.6, 4.7, 7.1, 7.2, 7.3, 7.4, 7.5, 7.8_

  - [ ]* 4.2 Escrever testes unitários para o domínio do client-registry
    - Testar invariante: `grant_types` incompatíveis com `response_types` lançam `InvalidClientMetadataException`
    - Testar invariante: `redirect_uris` não-HTTPS para cliente web lançam exceção tipada
    - Testar que `ClientRegistered`, `ClientUpdated`, `ClientDeleted` são gerados nas operações corretas
    - _Requirements: 3.12, 7.5, 7.6_

  - [ ]* 4.3 Escrever property test para invariantes do Client (Property 3)
    - **Property 3: Invariantes de domínio lançam exceção tipada**
    - Gerar combinações arbitrárias de `grant_types` e `response_types` incompatíveis; verificar que sempre lançam `InvalidClientMetadataException`
    - **Validates: Requirements 3.12, 7.5**
    - _Requirements: 3.12, 7.5_

  - [x] 4.4 Implementar Application Services do client-registry
    - Implementar `ClientRegistrationService` implementando todos os Use Case ports
    - Ao registrar cliente sem escopos explícitos, invocar `ScopeQueryPort.getDefaultScopes()` e atribuir ao cliente
    - Publicar Domain Events via `DomainEventPublisher` após cada operação bem-sucedida
    - _Requirements: 2.5, 4.5, 4.6, 4.7, 7.1, 7.2, 7.3, 7.4, 10.4_

  - [x] 4.5 Implementar adapter de persistência MongoDB do client-registry
    - Criar `MongoClientRepository` implementando `ClientRepository`
    - Criar documento `ClientDocument` com `@Document("clients")`, `_id` = `clientId`
    - Criar índice único em `clientId` via `@Indexed(unique=true)`
    - Implementar optimistic locking via campo `version` com `@Version`; lançar `OptimisticLockingException` em conflito
    - _Requirements: 5.1, 5.2, 5.5, 5.7_

  - [ ]* 4.6 Escrever teste de integração MongoDB para client-registry
    - Usar Testcontainers para testar `MongoClientRepository`
    - Verificar índice único em `clientId`
    - Testar optimistic locking: segunda escrita com `version` desatualizada lança `OptimisticLockingException`
    - _Requirements: 5.5, 5.7_

  - [ ]* 4.7 Escrever property test para optimistic locking (Property 4)
    - **Property 4: Conflito de versão lança exceção de concorrência tipada**
    - Gerar Clients arbitrários, persistir, simular escrita concorrente com `version` desatualizada, verificar `OptimisticLockingException` sem corrupção do documento
    - **Validates: Requirements 5.7**
    - _Requirements: 5.7_

  - [x] 4.8 Implementar adapter REST para scope-manager (ScopeQueryPort)
    - Criar `ScopeManagerRestAdapter` implementando `ScopeQueryPort` usando `RestClient` (Spring 6)
    - Configurar circuit breaker Resilience4j com threshold 50%, retry 3x com backoff exponencial, timeout 2s
    - Configurar fallback: usar cache local de escopos (TTL 5 minutos) em caso de indisponibilidade
    - _Requirements: 1.5, 10.3, 10.4_

  - [x] 4.9 Implementar adapter Kafka para Domain Events do client-registry
    - Criar `KafkaDomainEventPublisher` implementando `DomainEventPublisher`
    - Publicar `ClientRegistered` no tópico `client.registered`, `ClientUpdated` em `client.updated`, `ClientDeleted` em `client.deleted`
    - Configurar Dead Letter Topic `{topic}.DLT` para eventos que falham após 3 tentativas
    - Criar `KafkaConfig` com serialização JSON do envelope de evento
    - _Requirements: 4.5, 4.6, 4.7, 4.10, 4.11_

  - [x] 4.10 Implementar REST adapter do client-registry
    - Criar `ClientRegistrationController` com endpoints RFC 7591/7592: `POST /register`, `GET /register/{client_id}`, `PUT /register/{client_id}`, `DELETE /register/{client_id}`
    - Criar `GlobalExceptionHandler` mapeando exceções de domínio para respostas OAuth2 (RFC 6749)
    - Criar `SecurityConfig`: `POST /register` aberto; `GET/PUT/DELETE /register/{id}` exige Registration Access Token
    - Criar `application.yml` com porta 8081, nome do serviço, URI MongoDB, URI Kafka, URL do scope-manager
    - Criar classe `ClientRegistryApplication` com `@SpringBootApplication`
    - _Requirements: 1.4, 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8_

  - [x] 4.11 Checkpoint — client-registry
    - Garantir que todos os testes do client-registry passam. Verificar que `POST /register` cria cliente e publica evento Kafka. Perguntar ao usuário se há dúvidas antes de continuar.


- [x] 5. Implementar microserviço `authorization-server`
  - [x] 5.1 Criar estrutura de pacotes hexagonal e domínio do authorization-server
    - Criar `authorization-server/pom.xml` com dependências: spring-boot-starter-web, spring-boot-starter-data-mongodb, spring-boot-starter-security, spring-boot-starter-actuator, spring-kafka, nimbus-jose-jwt, bouncy-castle
    - Criar Aggregates: `AccessToken` (Root), `RefreshToken` (Root), `AuthorizationCode`, `DeviceCode`
    - Criar Value Objects imutáveis: `TokenValue` (JWT string), `ClientId`, `Subject`, `Scope`, `AuthenticationHolder`, `CodeValue`, `PKCEChallenge`, `DeviceCodeValue`, `UserCode`
    - Criar Domain Events: `AccessTokenIssued`, `AccessTokenRevoked`, `RefreshTokenIssued`, `RefreshTokenRevoked`
    - Criar exceções de domínio: `InvalidGrantException`, `TokenExpiredException`, `AuthorizationCodeReusedException`
    - Criar Driving Ports: `IssueTokenUseCase`, `RevokeTokenUseCase`, `IntrospectTokenUseCase`, `AuthorizeUseCase`, `DeviceAuthorizationUseCase`
    - Criar Driven Ports: `AccessTokenRepository`, `RefreshTokenRepository`, `AuthorizationCodeRepository`, `DeviceCodeRepository`, `ClientQueryPort`, `ScopeQueryPort`, `DomainEventPublisher`
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 3.2, 3.3, 3.4, 4.1, 4.2, 4.3, 4.4, 6.1_

  - [ ]* 5.2 Escrever testes unitários para o domínio do authorization-server
    - Testar `AuthorizationCode`: segunda utilização revoga todos os tokens associados e lança `AuthorizationCodeReusedException`
    - Testar `RefreshToken`: após uso, token marcado como inválido; segunda tentativa retorna `invalid_grant`
    - Testar `AccessToken`: token expirado lança `TokenExpiredException`
    - Testar que Domain Events corretos são gerados em cada operação
    - _Requirements: 3.12, 6.4, 6.9, 6.10_

  - [ ]* 5.3 Escrever property test para refresh token de uso único (Property 6)
    - **Property 6: Refresh token é invalidado após uso**
    - Gerar RefreshTokens válidos arbitrários; após uso para emitir AccessToken, verificar que segunda tentativa retorna `invalid_grant`
    - **Validates: Requirements 6.4**
    - _Requirements: 6.4_

  - [ ]* 5.4 Escrever property test para authorization code de uso único (Property 7)
    - **Property 7: Authorization code é de uso único**
    - Gerar AuthorizationCodes válidos arbitrários; após uso, verificar que segunda tentativa revoga tokens e retorna `invalid_grant`
    - **Validates: Requirements 6.9**
    - _Requirements: 6.9_

  - [x] 5.5 Implementar TokenService com Nimbus JOSE+JWT
    - Criar `TokenIssuanceService` implementando `IssueTokenUseCase`: emitir JWT assinado com RS256 usando keystore configurado
    - Criar `TokenSerializer` e `TokenParser` usando Nimbus JOSE+JWT para serialização/parse de AccessToken e RefreshToken
    - Implementar validação de assinatura JWT; retornar erro tipado para JWT inválido (string vazia, JSON arbitrário, assinatura inválida, algoritmo `none`)
    - Criar `TokenRevocationService` implementando `RevokeTokenUseCase`
    - Criar `TokenIntrospectionService` implementando `IntrospectTokenUseCase` (RFC 7662)
    - _Requirements: 6.7, 6.8, 11.1, 11.2, 11.3, 11.5, 11.6_

  - [ ]* 5.6 Escrever property test para round-trip de serialização JWT (Property 2)
    - **Property 2: Round-trip de serialização JWT**
    - Gerar AccessTokens válidos com claims arbitrários (clientId, sub, scopes aleatórios); verificar `parse(serialize(token)).claims == token.claims`
    - Usar `@Provide` com `Combinators.combine` para gerar tokens válidos
    - **Validates: Requirements 11.4**
    - _Requirements: 11.4_

  - [ ]* 5.7 Escrever property test para parse de JWT inválido (Property 5)
    - **Property 5: Parse de JWT inválido retorna erro tipado**
    - Gerar strings arbitrárias que não são JWTs válidos (strings vazias, JSON, JWTs com assinatura inválida); verificar que `TokenParser` retorna erro tipado sem `NullPointerException` ou `RuntimeException` genérica
    - **Validates: Requirements 11.5, 11.6**
    - _Requirements: 11.5, 11.6_

  - [x] 5.8 Implementar Application Services do authorization-server
    - Implementar `AuthorizationService` para fluxo Authorization Code com suporte a PKCE (RFC 7636)
    - Implementar `DeviceAuthorizationService` para Device Flow (RFC 8628)
    - Consultar `ClientQueryPort` para validar cliente em cada fluxo de token
    - Consultar `ScopeQueryPort` para validar escopos solicitados; rejeitar escopos `restricted` sem autorização
    - Publicar Domain Events via `DomainEventPublisher` após cada operação
    - _Requirements: 2.5, 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 6.9, 6.10, 10.5_

  - [x] 5.9 Implementar adapters de persistência MongoDB do authorization-server
    - Criar `MongoAccessTokenRepository`, `MongoRefreshTokenRepository`, `MongoAuthorizationCodeRepository`, `MongoDeviceCodeRepository`
    - Criar documentos com `@Document` e subdocumentos embutidos para `AuthenticationHolder`
    - Criar índices TTL em `expiration` (`expireAfterSeconds: 0`) para access_tokens, refresh_tokens, authorization_codes, device_codes
    - Criar índices adicionais conforme design: `clientId`, `authenticationHolder.userSub`, `refreshTokenId`, `approvedSiteId`, `code` (unique), `userCode` (unique), `deviceCode`
    - Implementar optimistic locking via `@Version` em todos os documentos
    - Suportar transações multi-documento MongoDB para emissão de token + invalidação de authorization code
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7_

  - [ ]* 5.10 Escrever property test para round-trip de persistência de Aggregate (Property 1)
    - **Property 1: Round-trip de persistência de Aggregate**
    - Gerar AccessTokens, RefreshTokens e AuthorizationCodes arbitrários; persistir e recuperar pelo ID; verificar que todos os campos são iguais ao original
    - Usar Testcontainers com MongoDB real
    - **Validates: Requirements 5.1**
    - _Requirements: 5.1_

  - [x] 5.11 Implementar adapters REST para client-registry e scope-manager
    - Criar `ClientRegistryRestAdapter` implementando `ClientQueryPort` com `RestClient` + Resilience4j
    - Criar `ScopeManagerRestAdapter` implementando `ScopeQueryPort` com `RestClient` + Resilience4j
    - Configurar circuit breaker, retry 3x, timeout 2s, fallback com cache local (TTL 5 min) para escopos
    - Consumir evento Kafka `client.updated` para invalidar cache local de clientes
    - _Requirements: 1.3, 1.5, 10.3_

  - [x] 5.12 Implementar adapter Kafka do authorization-server
    - Criar `KafkaDomainEventPublisher` publicando eventos nos tópicos: `auth.token.issued`, `auth.token.revoked`, `auth.refresh.issued`, `auth.refresh.revoked`
    - Configurar Dead Letter Topic para eventos com falha após 3 tentativas
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.10, 4.11_

  - [x] 5.13 Implementar REST adapter do authorization-server
    - Criar `AuthorizationEndpoint` (`GET/POST /authorize`), `TokenEndpoint` (`POST /token`), `RevocationEndpoint` (`POST /revoke`), `IntrospectionEndpoint` (`POST /introspect`), `DeviceAuthorizationEndpoint` (`POST /device_authorization`)
    - Criar `GlobalExceptionHandler` mapeando exceções de domínio para respostas OAuth2 (RFC 6749)
    - Criar `SecurityConfig` com autenticação de cliente (Basic Auth, client_secret_post, private_key_jwt) nos endpoints `/token`, `/introspect`, `/revoke`
    - Criar `application.yml` com porta 8080, URI MongoDB, URI Kafka, URLs de scope-manager e client-registry
    - Criar classe `AuthorizationServerApplication` com `@SpringBootApplication`
    - _Requirements: 1.4, 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 6.9, 6.10_

  - [x] 5.14 Checkpoint — authorization-server
    - Garantir que todos os testes do authorization-server passam. Verificar fluxo authorization_code end-to-end com mocks. Perguntar ao usuário se há dúvidas antes de continuar.


- [x] 6. Implementar microserviço `oidc-provider`
  - [x] 6.1 Criar estrutura de pacotes hexagonal e domínio do oidc-provider
    - Criar `oidc-provider/pom.xml` com dependências: spring-boot-starter-web, spring-boot-starter-data-mongodb, spring-boot-starter-security, spring-boot-starter-actuator, spring-kafka, nimbus-jose-jwt, bouncy-castle
    - Criar `UserInfo` Aggregate Root com Value Objects: `Subject`, `Address` (embutido: formatted, streetAddress, locality, region, postalCode, country)
    - Criar `PairwiseIdentifier` Aggregate com Value Objects: `UserSub`, `SectorIdentifier`, `PairwiseValue`; invariante: par `(userSub, sectorIdentifier)` é único
    - Criar Aggregates de política de site: `ApprovedSite`, `WhitelistedSite`, `BlacklistedSite`
    - Criar exceções de domínio: `UserInfoNotFoundException`, `PairwiseIdentifierConflictException`
    - Criar Driving Ports: `GetUserInfoUseCase`, `GetDiscoveryDocumentUseCase`, `GetJwksUseCase`, `GeneratePairwiseIdentifierUseCase`, `EndSessionUseCase`
    - Criar Driven Ports: `UserInfoRepository`, `PairwiseIdentifierRepository`, `ApprovedSiteRepository`, `TokenIntrospectionPort` (chama authorization-server), `DomainEventPublisher`
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 3.5, 3.6, 3.7, 3.8, 8.1, 8.5_

  - [ ]* 6.2 Escrever testes unitários para o domínio do oidc-provider
    - Testar `PairwiseIdentifier`: geração determinística para mesmo par `(userSub, sectorIdentifier)`
    - Testar `UserInfo`: campos obrigatórios e Value Object `Address` imutável
    - _Requirements: 3.12, 8.5_

  - [ ]* 6.3 Escrever property test para pairwise identifier determinístico (Property 8)
    - **Property 8: Pairwise identifier é determinístico**
    - Gerar pares arbitrários `(userSub, sectorIdentifier)`; chamar `GeneratePairwiseIdentifierUseCase` múltiplas vezes para o mesmo par; verificar que sempre retorna o mesmo `PairwiseValue`
    - **Validates: Requirements 8.5**
    - _Requirements: 8.5_

  - [x] 6.4 Implementar Application Services do oidc-provider
    - Implementar `UserInfoService` implementando `GetUserInfoUseCase`: validar access token via `TokenIntrospectionPort`, retornar UserInfo filtrado pelos escopos do token
    - Implementar `DiscoveryService` implementando `GetDiscoveryDocumentUseCase`: montar documento de discovery conforme OpenID Connect Discovery 1.0
    - Implementar `JwksService` implementando `GetJwksUseCase`: expor chaves públicas JWK
    - Implementar `PairwiseService` implementando `GeneratePairwiseIdentifierUseCase`: gerar ou recuperar PairwiseIdentifier para par `(userSub, sectorIdentifier)`
    - Consumir evento Kafka `client.registered` para setup de pairwise quando `subject_type=pairwise`
    - _Requirements: 2.5, 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7, 8.8, 8.9_

  - [x] 6.5 Implementar adapters de persistência MongoDB do oidc-provider
    - Criar `MongoUserInfoRepository`, `MongoPairwiseIdentifierRepository`, `MongoApprovedSiteRepository`, `MongoWhitelistedSiteRepository`, `MongoBlacklistedSiteRepository`
    - Criar documentos com `@Document` e subdocumentos embutidos para `Address`
    - Criar índices: `user_info` — único em `preferredUsername`, em `email`; `pairwise_identifiers` — único composto em `(userSub, sectorIdentifier)`; `approved_sites` — composto em `(userId, clientId)`; `whitelisted_sites` — em `clientId`
    - _Requirements: 5.1, 5.2, 5.3, 5.5_

  - [x] 6.6 Implementar adapter REST para authorization-server (TokenIntrospectionPort)
    - Criar `AuthorizationServerRestAdapter` implementando `TokenIntrospectionPort` com `RestClient` + Resilience4j
    - Usar mTLS ou service account token para autenticação da chamada interna
    - _Requirements: 1.3, 1.5_

  - [x] 6.7 Implementar consumer Kafka do oidc-provider
    - Criar consumer para tópico `client.registered` para processar setup de pairwise
    - Criar consumer para tópico `auth.token.issued` para auditoria
    - Configurar Dead Letter Topic para mensagens com falha
    - _Requirements: 4.10, 4.11_

  - [x] 6.8 Implementar REST adapter do oidc-provider
    - Criar `UserInfoEndpoint` (`GET/POST /userinfo`), `DiscoveryEndpoint` (`GET /.well-known/openid-configuration`), `JwksEndpoint` (`GET /jwks`), `EndSessionEndpoint` (`GET/POST /end_session`)
    - Suportar resposta UserInfo como JWT assinado (`userinfo_signed_response_alg`) e criptografado (`userinfo_encrypted_response_alg`) usando Nimbus JOSE+JWT
    - Criar `SecurityConfig`: `/userinfo` exige Bearer token com escopo `openid`; demais endpoints públicos
    - Criar `application.yml` com porta 8082, URI MongoDB, URI Kafka, URL do authorization-server
    - Criar classe `OidcProviderApplication` com `@SpringBootApplication`
    - _Requirements: 1.4, 8.1, 8.2, 8.3, 8.4, 8.6, 8.7, 8.8, 8.9_

  - [x] 6.9 Checkpoint — oidc-provider
    - Garantir que todos os testes do oidc-provider passam. Verificar que `GET /.well-known/openid-configuration` retorna documento de discovery correto. Perguntar ao usuário se há dúvidas antes de continuar.

- [x] 7. Implementar microserviço `uma-server`
  - [x] 7.1 Criar estrutura de pacotes hexagonal e domínio do uma-server
    - Criar `uma-server/pom.xml` com dependências: spring-boot-starter-web, spring-boot-starter-data-mongodb, spring-boot-starter-security, spring-boot-starter-actuator, spring-kafka, nimbus-jose-jwt
    - Criar `ResourceSet` Aggregate Root com entidades internas `Policy` e `Claim`; Value Objects: `ResourceSetId`, `Owner`, `ClientId`
    - Criar `PermissionTicket` Aggregate Root com Value Objects: `TicketValue`, `Permission` (resourceSetId + scopes), `ClaimsSupplied`; invariante: expira após TTL configurado
    - Criar Domain Events: `ResourceSetRegistered`, `ResourceSetDeleted`, `PermissionTicketCreated`
    - Criar exceções de domínio: `ResourceSetNotFoundException`, `PermissionTicketExpiredException`, `InsufficientClaimsException`
    - Criar Driving Ports: `RegisterResourceSetUseCase`, `GetResourceSetUseCase`, `UpdateResourceSetUseCase`, `DeleteResourceSetUseCase`, `CreatePermissionTicketUseCase`, `IssueRptUseCase`
    - Criar Driven Ports: `ResourceSetRepository`, `PermissionTicketRepository`, `DomainEventPublisher`
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 3.9, 3.10, 4.8, 4.9, 9.1_

  - [ ]* 7.2 Escrever testes unitários para o domínio do uma-server
    - Testar `PermissionTicket`: ticket expirado lança `PermissionTicketExpiredException`
    - Testar `ResourceSet`: deleção invalida PermissionTickets associados
    - Testar avaliação de `Policy`: claims insuficientes lançam `InsufficientClaimsException`
    - _Requirements: 3.12, 9.7, 9.8, 9.9_

  - [x] 7.3 Implementar Application Services do uma-server
    - Implementar `ResourceSetService` para CRUD de ResourceSet
    - Implementar `PermissionTicketService` para criação de PermissionTicket
    - Implementar `RptIssuanceService` para emissão de RPT: avaliar Policies do ResourceSet, verificar Claims fornecidos
    - Ao deletar ResourceSet, invalidar todos os PermissionTickets associados
    - Publicar Domain Events via `DomainEventPublisher`
    - _Requirements: 2.5, 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7, 9.8, 9.9_

  - [x] 7.4 Implementar adapters de persistência MongoDB do uma-server
    - Criar `MongoResourceSetRepository`, `MongoPermissionTicketRepository`
    - Criar documentos com `@Document`; `Policy` e `Claim` como subdocumentos embutidos em `resource_sets`; `Permission` e `ClaimsSupplied` embutidos em `permission_tickets`
    - Criar índices: `resource_sets` — em `owner`, composto em `(owner, clientId)`, em `clientId`; `permission_tickets` — único em `ticket`, TTL em `expiration`
    - Implementar optimistic locking via `@Version`
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.7_

  - [x] 7.5 Implementar adapter Kafka do uma-server
    - Criar `KafkaDomainEventPublisher` publicando eventos: `uma.resource.registered`, `uma.permission.created`
    - Criar consumer para tópico `auth.token.revoked` para invalidar RPTs associados ao token revogado
    - Configurar Dead Letter Topic
    - _Requirements: 4.8, 4.9, 4.10, 4.11_

  - [x] 7.6 Implementar REST adapter do uma-server
    - Criar `ResourceSetEndpoint` (`POST/GET/PUT/DELETE /uma/resource_set`, `GET /uma/resource_set`) e `PermissionEndpoint` (`POST /uma/permission`)
    - Criar `GlobalExceptionHandler` mapeando exceções de domínio para respostas UMA
    - Criar `SecurityConfig`: todos os endpoints UMA exigem Bearer token com escopo UMA
    - Criar `application.yml` com porta 8083, URI MongoDB, URI Kafka
    - Criar classe `UmaServerApplication` com `@SpringBootApplication`
    - _Requirements: 1.4, 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7_

  - [x] 7.7 Checkpoint — uma-server
    - Garantir que todos os testes do uma-server passam. Verificar que `POST /uma/resource_set` cria ResourceSet e publica evento. Perguntar ao usuário se há dúvidas antes de continuar.


- [x] 8. Implementar API Gateway
  - [x] 8.1 Criar módulo api-gateway com Spring Cloud Gateway
    - Criar `api-gateway/pom.xml` com dependências: spring-cloud-starter-gateway, spring-boot-starter-actuator, spring-cloud-starter-circuitbreaker-resilience4j
    - Criar `application.yml` com rotas:
      - `/authorize`, `/token`, `/revoke`, `/introspect`, `/device_authorization` → `authorization-server:8080`
      - `/register`, `/register/**` → `client-registry:8081`
      - `/userinfo`, `/.well-known/openid-configuration`, `/jwks`, `/end_session` → `oidc-provider:8082`
      - `/uma/**` → `uma-server:8083`
      - `/scopes/**` → `scope-manager:8084`
    - Configurar circuit breaker Resilience4j por rota; retornar HTTP 503 em caso de falha do serviço destino
    - Criar `ApiGatewayApplication` com `@SpringBootApplication`
    - _Requirements: 1.4, 1.5_

  - [ ]* 8.2 Escrever testes de integração para roteamento do API Gateway
    - Testar que cada rota pública é corretamente encaminhada ao microserviço destino (usando WireMock ou MockServer)
    - Testar que circuit breaker retorna 503 quando serviço destino está indisponível
    - _Requirements: 1.4, 1.5_

- [x] 9. Implementar ferramenta de migração de dados (`migration-tool`)
  - [x] 9.1 Criar módulo migration-tool com Spring Batch
    - Criar `migration-tool/pom.xml` com dependências: spring-boot-starter-batch, spring-boot-starter-data-mongodb, spring-boot-starter-data-jpa, driver JDBC (H2/MySQL/PostgreSQL), nimbus-jose-jwt
    - Criar `MigrationToolApplication` com `@SpringBootApplication` e `@EnableBatchProcessing`
    - Criar `application.yml` com configuração de datasource SQL (origem) e MongoDB (destino)
    - _Requirements: 12.1_

  - [x] 9.2 Implementar jobs de migração por coleção (ordem de dependências)
    - Implementar `SystemScopeMigrationJob`: lê `system_scope` SQL → escreve `system_scopes` MongoDB (upsert por `value`)
    - Implementar `ClientMigrationJob`: lê `client_details` + tabelas `@ElementCollection` → escreve `clients` MongoDB (upsert por `clientId`), desnormalizando arrays
    - Implementar `UserInfoMigrationJob`: lê `user_info` + `address` → escreve `user_info` MongoDB com `address` como subdocumento
    - Implementar `PairwiseMigrationJob`, `ApprovedSiteMigrationJob`, `WhitelistedSiteMigrationJob`, `BlacklistedSiteMigrationJob`
    - Implementar `AuthenticationHolderMigrationJob`: desnormaliza holders inline nos tokens
    - Implementar `RefreshTokenMigrationJob`, `AuthorizationCodeMigrationJob`, `DeviceCodeMigrationJob`
    - Implementar `AccessTokenMigrationJob`: lê `access_token` + `token_scope` → escreve `access_tokens` MongoDB
    - Implementar `ResourceSetMigrationJob`: lê `resource_set` + `policy` + `claim` → escreve `resource_sets` com policies e claims embutidos
    - Implementar `PermissionTicketMigrationJob`
    - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5_

  - [x] 9.3 Implementar relatório de migração e tratamento de erros
    - Criar `MigrationReportWriter` que grava `migration-report.json` com: contagem de documentos migrados por coleção, registros com erro (ID + causa), discrepâncias SQL vs MongoDB
    - Implementar `SkipPolicy` no Spring Batch: registros com dados inconsistentes são pulados e registrados no relatório; migração continua
    - Garantir idempotência: usar `replaceOne({ _id: <id> }, doc, { upsert: true })` em todos os writers
    - _Requirements: 12.3, 12.4, 12.5_

  - [x] 9.4 Escrever property test para idempotência da migração (Property 9)
    - **Property 9: Migração é idempotente**
    - Gerar conjuntos arbitrários de registros SQL de entrada; executar migração 1x e 2x; verificar que o estado final do MongoDB é idêntico (sem duplicação, sem perda, contagem igual)
    - **Validates: Requirements 12.5**
    - _Requirements: 12.5_

  - [x] 9.5 Checkpoint — migration-tool
    - Garantir que todos os testes da migration-tool passam. Verificar que execução múltipla produz mesmo resultado. Perguntar ao usuário se há dúvidas antes de continuar.

- [x] 10. Configurar observabilidade em todos os microserviços
  - [x] 10.1 Adicionar Actuator, Micrometer e OpenTelemetry a cada microserviço
    - Adicionar dependências ao `pom.xml` de cada microserviço: `spring-boot-starter-actuator`, `micrometer-registry-prometheus`, `micrometer-tracing-bridge-otel`, `opentelemetry-exporter-otlp`
    - Configurar `application.yml` de cada serviço: expor `/actuator/health`, `/actuator/metrics`, `/actuator/info`; incluir health indicators para MongoDB e Kafka
    - Configurar Logback com `logstash-logback-encoder` para logs JSON com campos: `timestamp`, `level`, `service`, `traceId`, `spanId`, `message`
    - Configurar propagação de `traceId` via headers HTTP (W3C Trace Context) em todas as chamadas REST entre serviços
    - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5_

- [x] 11. Criar Docker Compose para orquestração local
  - [x] 11.1 Criar `docker-compose.yml` na raiz do projeto
    - Definir serviços: `mongodb-auth`, `mongodb-client`, `mongodb-oidc`, `mongodb-uma`, `mongodb-scope` (5 instâncias MongoDB independentes, portas 27017-27021)
    - Definir serviço `kafka` com Zookeeper (ou KRaft) e criação automática dos tópicos: `auth.token.issued`, `auth.token.revoked`, `auth.refresh.issued`, `auth.refresh.revoked`, `client.registered`, `client.updated`, `client.deleted`, `uma.resource.registered`, `uma.permission.created` e respectivos `.DLT`
    - Definir serviços para cada microserviço com `build: ./{service-name}`, variáveis de ambiente apontando para os containers MongoDB e Kafka, e `depends_on` corretos
    - Definir serviço `api-gateway` expondo porta 443 (ou 8443) com `depends_on` em todos os microserviços
    - Definir serviço `migration-tool` com `restart: no` e `depends_on` nos containers MongoDB
    - _Requirements: 1.1, 1.2_

  - [x] 11.2 Criar `Dockerfile` para cada microserviço
    - Criar `Dockerfile` multi-stage em cada módulo: stage `build` com Maven 3.9 + Java 21; stage `runtime` com `eclipse-temurin:21-jre-alpine`
    - Copiar apenas o JAR final no stage runtime; expor a porta correta de cada serviço
    - _Requirements: 1.1_

- [ ] 12. Checkpoint final — integração completa
  - Garantir que todos os testes unitários, de propriedade e de integração passam em todos os módulos (`mvn test` no módulo raiz)
  - Verificar que `docker-compose up` sobe todos os serviços sem erros
  - Verificar que `GET /.well-known/openid-configuration` via API Gateway retorna discovery document correto
  - Perguntar ao usuário se há ajustes finais antes de encerrar.

## Notas

- Tarefas marcadas com `*` são opcionais e podem ser puladas para um MVP mais rápido
- A ordem das tarefas respeita as dependências: scope-manager → client-registry → authorization-server → oidc-provider → uma-server
- Cada tarefa referencia os requisitos específicos para rastreabilidade
- As 9 propriedades de corretude do design são cobertas pelos property tests com jqwik: Properties 1-9
- Checkpoints garantem validação incremental a cada microserviço concluído
