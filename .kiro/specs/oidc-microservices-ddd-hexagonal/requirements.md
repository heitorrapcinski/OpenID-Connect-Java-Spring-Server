# Documento de Requisitos

## Introdução

Este documento descreve os requisitos para a refatoração do MITREid Connect
(openid-connect-server-webapp) — implementação de referência de OpenID Connect,
OAuth 2.0 e UMA em Java/Spring — de uma arquitetura monolítica para uma
arquitetura baseada em microserviços com Domain-Driven Design (DDD) e
arquitetura hexagonal (Ports & Adapters), substituindo a persistência
JPA/SQL (HSQLDB, MySQL, PostgreSQL, Oracle) por MongoDB.

Todas as funcionalidades existentes devem ser preservadas. A refatoração
organiza o sistema em Bounded Contexts independentes, cada um implantável
como um microserviço autônomo com sua própria base de dados MongoDB.

---

## Glossário

- **Authorization_Server**: O microserviço responsável pelos fluxos OAuth 2.0
  (Authorization Code, Client Credentials, Implicit, Device Flow, PKCE).
- **Token_Service**: Componente interno do Authorization_Server que emite,
  valida e revoga tokens de acesso e refresh (JWT).
- **Client_Registry**: O microserviço responsável pelo registro e gerenciamento
  de clientes OAuth 2.0 (Dynamic Client Registration — RFC 7591/7592).
- **OIDC_Provider**: O microserviço responsável pelos fluxos OpenID Connect
  (ID Token, UserInfo endpoint, Discovery, Session Management).
- **UMA_Server**: O microserviço responsável pelo protocolo UMA 2.0
  (Resource Registration, Permission Ticket, Requesting Party Token).
- **Scope_Manager**: O microserviço responsável pelo gerenciamento de escopos
  do sistema (SystemScope).
- **Site_Policy_Service**: O microserviço responsável pelas políticas de
  aprovação de sites (ApprovedSite, WhitelistedSite, BlacklistedSite).
- **Aggregate**: Cluster de objetos de domínio tratado como uma unidade para
  fins de consistência e persistência (DDD).
- **Aggregate_Root**: Entidade raiz de um Aggregate, único ponto de entrada
  para modificações.
- **Domain_Event**: Evento imutável que representa algo que ocorreu no domínio.
- **Port**: Interface que define um contrato de entrada (driving port) ou saída
  (driven port) na arquitetura hexagonal.
- **Adapter**: Implementação concreta de um Port que conecta o domínio a
  tecnologias externas (HTTP, MongoDB, mensageria).
- **MongoDB_Repository**: Adapter de saída que implementa um Port de repositório
  usando Spring Data MongoDB.
- **JWT**: JSON Web Token (RFC 7519), formato de token usado para access tokens
  e ID tokens.
- **PKCE**: Proof Key for Code Exchange (RFC 7636), extensão de segurança para
  o fluxo Authorization Code.
- **UMA**: User-Managed Access 2.0, protocolo de autorização delegada.
- **ResourceSet**: Aggregate raiz do contexto UMA que representa um recurso
  protegido registrado por um Resource Owner.
- **PermissionTicket**: Aggregate raiz do contexto UMA que representa uma
  solicitação de acesso pendente.
- **ClientDetailsEntity**: Entidade legada que representa um cliente OAuth 2.0;
  no novo modelo é o Aggregate_Root do contexto Client_Registry.
- **AuthenticationHolder**: Value Object que encapsula o contexto de
  autenticação associado a um token.
- **PairwiseIdentifier**: Value Object que representa o identificador de sujeito
  pairwise gerado para um usuário em relação a um sector identifier.
- **DeviceCode**: Aggregate que representa um código de dispositivo pendente de
  aprovação no Device Authorization Flow (RFC 8628).
- **Event_Bus**: Componente de infraestrutura para publicação e consumo de
  Domain_Events entre microserviços (ex.: Apache Kafka ou RabbitMQ).

---

## Requisitos

### Requisito 1: Decomposição em Bounded Contexts e Microserviços

**User Story:** Como arquiteto de software, quero que o sistema seja decomposto
em microserviços independentes por Bounded Context, para que cada serviço possa
ser desenvolvido, implantado e escalado de forma autônoma.

#### Critérios de Aceitação

1. THE System SHALL ser decomposto nos seguintes Bounded Contexts independentes:
   `authorization-server`, `client-registry`, `oidc-provider`,
   `uma-server` e `scope-manager`.
2. THE System SHALL garantir que cada Bounded Context possua seu próprio banco
   de dados MongoDB dedicado, sem compartilhamento de coleções entre serviços.
3. WHEN um Bounded Context precisar de dados de outro Bounded Context, THE
   System SHALL obter esses dados via chamada de API REST ou via consumo de
   Domain_Events publicados no Event_Bus, nunca por acesso direto ao banco de
   dados de outro serviço.
4. THE System SHALL expor as mesmas URLs de endpoint públicas do monolito
   original (ex.: `/authorize`, `/token`, `/userinfo`, `/introspect`,
   `/register`, `/uma/...`) para garantir compatibilidade retroativa com
   clientes existentes.
5. IF um microserviço ficar indisponível, THEN THE System SHALL retornar
   respostas de erro HTTP adequadas (503 Service Unavailable) aos clientes,
   sem propagar falhas em cascata para outros microserviços.

---

### Requisito 2: Arquitetura Hexagonal em Cada Microserviço

**User Story:** Como desenvolvedor, quero que cada microserviço siga a
arquitetura hexagonal (Ports & Adapters), para que a lógica de domínio seja
completamente isolada de frameworks, bancos de dados e protocolos de transporte.

#### Critérios de Aceitação

1. THE System SHALL organizar cada microserviço em três camadas: `domain`
   (Aggregates, Value Objects, Domain Events, Ports), `application`
   (Use Cases / Application Services) e `infrastructure` (Adapters HTTP,
   MongoDB, Event_Bus).
2. THE System SHALL definir Ports de entrada (driving ports) como interfaces
   Java que os Use Cases implementam, invocadas pelos Adapters HTTP (controllers
   REST).
3. THE System SHALL definir Ports de saída (driven ports) como interfaces Java
   que os Adapters de infraestrutura implementam (ex.: `ClientRepository`,
   `TokenRepository`), invocadas pelos Use Cases.
4. THE System SHALL garantir que nenhuma classe da camada `domain` importe
   classes de frameworks externos (Spring, MongoDB, Jackson), exceto anotações
   de valor semântico neutro.
5. WHEN um Use Case precisar persistir ou recuperar um Aggregate, THE
   Application_Service SHALL invocar exclusivamente o Port de repositório
   correspondente, nunca o MongoDB_Repository diretamente.
6. THE System SHALL garantir que os Use Cases sejam testáveis com mocks dos
   Ports de saída, sem necessidade de banco de dados ou servidor HTTP em
   execução.

---

### Requisito 3: Modelagem DDD — Aggregates e Value Objects

**User Story:** Como desenvolvedor, quero que os objetos de domínio sejam
modelados como Aggregates e Value Objects segundo DDD, para que as regras de
negócio sejam encapsuladas no domínio e a consistência seja garantida pelos
Aggregate Roots.

#### Critérios de Aceitação

1. THE System SHALL modelar `Client` (derivado de `ClientDetailsEntity`) como
   Aggregate_Root do contexto `client-registry`, com `ClientId` como Value
   Object de identidade.
2. THE System SHALL modelar `AccessToken` (derivado de
   `OAuth2AccessTokenEntity`) e `RefreshToken` (derivado de
   `OAuth2RefreshTokenEntity`) como Aggregates do contexto
   `authorization-server`, com `TokenValue` (JWT serializado) como Value Object
   imutável.
3. THE System SHALL modelar `AuthorizationCode` (derivado de
   `AuthorizationCodeEntity`) como Aggregate do contexto
   `authorization-server`, com `CodeValue` e `AuthenticationHolder` como Value
   Objects.
4. THE System SHALL modelar `DeviceCode` como Aggregate do contexto
   `authorization-server`, contendo `UserCode`, `DeviceCodeValue` e `Scope`
   como Value Objects.
5. THE System SHALL modelar `UserInfo` (derivado de `DefaultUserInfo`) como
   Aggregate_Root do contexto `oidc-provider`, com `Subject` como Value Object
   de identidade e `Address` como Value Object embutido.
6. THE System SHALL modelar `ApprovedSite` como Aggregate_Root do contexto
   `site-policy`, com `UserId`, `ClientId` e `AllowedScopes` como Value
   Objects.
7. THE System SHALL modelar `WhitelistedSite` e `BlacklistedSite` como
   Aggregates do contexto `site-policy`.
8. THE System SHALL modelar `PairwiseIdentifier` como Aggregate do contexto
   `oidc-provider`, com `UserSub` e `SectorIdentifier` como Value Objects.
9. THE System SHALL modelar `ResourceSet` como Aggregate_Root do contexto
   `uma-server`, contendo `Policy` e `Claim` como entidades internas do
   Aggregate.
10. THE System SHALL modelar `PermissionTicket` como Aggregate_Root do contexto
    `uma-server`, contendo `Permission` como Value Object.
11. THE System SHALL modelar `SystemScope` como Aggregate_Root do contexto
    `scope-manager`, com `ScopeValue` como Value Object de identidade.
12. IF uma operação violar uma invariante de negócio de um Aggregate (ex.:
    emitir token para cliente inexistente), THEN THE Aggregate_Root SHALL lançar
    uma exceção de domínio tipada, sem expor detalhes de infraestrutura.

---

### Requisito 4: Domain Events

**User Story:** Como arquiteto, quero que mudanças de estado relevantes nos
Aggregates gerem Domain Events, para que outros Bounded Contexts possam reagir
de forma desacoplada.

#### Critérios de Aceitação

1. THE Authorization_Server SHALL publicar o Domain_Event `AccessTokenIssued`
   no Event_Bus quando um access token for emitido com sucesso.
2. THE Authorization_Server SHALL publicar o Domain_Event `AccessTokenRevoked`
   no Event_Bus quando um access token for revogado.
3. THE Authorization_Server SHALL publicar o Domain_Event `RefreshTokenIssued`
   no Event_Bus quando um refresh token for emitido.
4. THE Authorization_Server SHALL publicar o Domain_Event `RefreshTokenRevoked`
   no Event_Bus quando um refresh token for revogado ou expirado.
5. THE Client_Registry SHALL publicar o Domain_Event `ClientRegistered` no
   Event_Bus quando um novo cliente for registrado dinamicamente.
6. THE Client_Registry SHALL publicar o Domain_Event `ClientUpdated` no
   Event_Bus quando os metadados de um cliente forem atualizados.
7. THE Client_Registry SHALL publicar o Domain_Event `ClientDeleted` no
   Event_Bus quando um cliente for removido.
8. THE UMA_Server SHALL publicar o Domain_Event `ResourceSetRegistered` no
   Event_Bus quando um ResourceSet for registrado.
9. THE UMA_Server SHALL publicar o Domain_Event `PermissionTicketCreated` no
   Event_Bus quando um PermissionTicket for criado.
10. WHEN um Domain_Event for publicado, THE Event_Bus SHALL garantir entrega
    at-least-once ao(s) consumidor(es) registrado(s).
11. IF a publicação de um Domain_Event falhar, THEN THE System SHALL registrar
    o evento em uma fila de dead-letter para reprocessamento posterior, sem
    reverter a transação de domínio principal.

---

### Requisito 5: Persistência com MongoDB

**User Story:** Como engenheiro de dados, quero que todos os Aggregates sejam
persistidos no MongoDB, para que o sistema se beneficie de esquema flexível,
escalabilidade horizontal e suporte nativo a documentos aninhados.

#### Critérios de Aceitação

1. THE MongoDB_Repository SHALL persistir cada Aggregate_Root como um documento
   MongoDB em sua coleção dedicada, usando o identificador do Aggregate como
   `_id` do documento.
2. THE System SHALL eliminar todas as dependências de JPA/EclipseLink,
   HSQLDB, MySQL, PostgreSQL e Oracle do código de produção.
3. THE MongoDB_Repository SHALL mapear Value Objects aninhados (ex.:
   `AuthenticationHolder`, `Address`, `Permission`) como subdocumentos
   embutidos no documento do Aggregate_Root, evitando referências entre
   coleções quando o Value Object não tiver identidade própria.
4. THE MongoDB_Repository SHALL armazenar tokens JWT (`AccessToken`,
   `RefreshToken`) com índice TTL (Time-To-Live) no campo `expiration`, para
   que o MongoDB remova automaticamente documentos expirados.
5. THE MongoDB_Repository SHALL criar índices nas coleções conforme as
   consultas mais frequentes do sistema legado:
   - `access_tokens`: índice em `clientId`, `authenticationHolder.userSub`,
     `refreshTokenId`, `approvedSiteId`;
   - `refresh_tokens`: índice em `clientId`, `authenticationHolder.userSub`;
   - `authorization_codes`: índice único em `code`;
   - `device_codes`: índice único em `userCode`, índice em `deviceCode`;
   - `clients`: índice único em `clientId`;
   - `user_info`: índice único em `preferredUsername`, índice em `email`;
   - `approved_sites`: índice composto em `(userId, clientId)`;
   - `whitelisted_sites`: índice em `clientId`;
   - `resource_sets`: índice em `owner`, índice composto em `(owner, clientId)`;
   - `permission_tickets`: índice único em `ticket`.
6. THE System SHALL suportar transações multi-documento do MongoDB (sessões)
   para operações que exijam consistência entre múltiplos Aggregates dentro do
   mesmo Bounded Context (ex.: emissão de access token + invalidação de
   authorization code).
7. IF uma operação de escrita no MongoDB falhar por conflito de versão
   (optimistic locking via campo `version`), THEN THE MongoDB_Repository SHALL
   lançar uma exceção de concorrência tipada para que o Use Case possa tratar
   o conflito.

---

### Requisito 6: Microserviço `authorization-server`

**User Story:** Como desenvolvedor de aplicações, quero que o servidor de
autorização OAuth 2.0 seja um microserviço independente, para que eu possa
obter tokens de acesso usando os fluxos padrão do OAuth 2.0.

#### Critérios de Aceitação

1. WHEN um cliente enviar uma requisição válida ao endpoint `/authorize`, THE
   Authorization_Server SHALL iniciar o fluxo Authorization Code e redirecionar
   o usuário para a tela de consentimento.
2. WHEN um cliente enviar uma requisição válida ao endpoint `/token` com
   `grant_type=authorization_code`, THE Authorization_Server SHALL emitir um
   `AccessToken` JWT e, se aplicável, um `RefreshToken` JWT.
3. WHEN um cliente enviar uma requisição válida ao endpoint `/token` com
   `grant_type=client_credentials`, THE Authorization_Server SHALL emitir um
   `AccessToken` JWT sem `RefreshToken`.
4. WHEN um cliente enviar uma requisição válida ao endpoint `/token` com
   `grant_type=refresh_token`, THE Authorization_Server SHALL emitir um novo
   `AccessToken` JWT e invalidar o `RefreshToken` anterior.
5. WHEN um cliente enviar uma requisição válida ao endpoint `/token` com
   `grant_type=urn:ietf:params:oauth:grant-type:device_code`, THE
   Authorization_Server SHALL completar o Device Authorization Flow (RFC 8628).
6. WHEN um cliente enviar uma requisição de autorização com `code_challenge` e
   `code_challenge_method`, THE Authorization_Server SHALL validar o PKCE
   (RFC 7636) antes de emitir o `AccessToken`.
7. WHEN um cliente enviar uma requisição ao endpoint `/revoke`, THE
   Authorization_Server SHALL revogar o token especificado e retornar HTTP 200.
8. WHEN um cliente enviar uma requisição ao endpoint `/introspect`, THE
   Authorization_Server SHALL retornar o estado atual do token (ativo/inativo)
   e seus metadados conforme RFC 7662.
9. IF um `AuthorizationCode` for utilizado mais de uma vez, THEN THE
   Authorization_Server SHALL revogar todos os tokens emitidos com base nesse
   código e retornar HTTP 400 com `error=invalid_grant`.
10. WHEN um `AccessToken` ou `RefreshToken` expirar, THE Authorization_Server
    SHALL retornar HTTP 401 com `error=invalid_token` em qualquer endpoint
    protegido.

---

### Requisito 7: Microserviço `client-registry`

**User Story:** Como desenvolvedor de aplicações, quero registrar e gerenciar
clientes OAuth 2.0 dinamicamente, para que minha aplicação possa obter
credenciais de cliente sem intervenção manual do administrador.

#### Critérios de Aceitação

1. WHEN um cliente enviar uma requisição POST válida ao endpoint `/register`,
   THE Client_Registry SHALL criar um novo `Client` Aggregate, gerar um
   `client_id` único e retornar os metadados do cliente com HTTP 201.
2. WHEN um cliente enviar uma requisição GET ao endpoint
   `/register/{client_id}` com um Registration Access Token válido, THE
   Client_Registry SHALL retornar os metadados atuais do cliente com HTTP 200.
3. WHEN um cliente enviar uma requisição PUT ao endpoint
   `/register/{client_id}` com um Registration Access Token válido, THE
   Client_Registry SHALL atualizar os metadados do `Client` Aggregate e
   retornar os metadados atualizados com HTTP 200.
4. WHEN um cliente enviar uma requisição DELETE ao endpoint
   `/register/{client_id}` com um Registration Access Token válido, THE
   Client_Registry SHALL remover o `Client` Aggregate e retornar HTTP 204.
5. IF um cliente tentar registrar `grant_types` incompatíveis com
   `response_types`, THEN THE Client_Registry SHALL retornar HTTP 400 com
   `error=invalid_client_metadata`.
6. THE Client_Registry SHALL validar que `redirect_uris` registradas usam
   esquema HTTPS para clientes do tipo `web`, exceto para URIs `localhost`
   em ambiente de desenvolvimento.
7. WHEN um `Client` for registrado com `subject_type=pairwise`, THE
   Client_Registry SHALL armazenar o `sector_identifier_uri` e notificar o
   `oidc-provider` via Domain_Event `ClientRegistered`.
8. THE Client_Registry SHALL suportar os campos de metadados definidos em
   RFC 7591, RFC 7592 e OpenID Connect Dynamic Registration 1.0, incluindo
   campos de criptografia JWE/JWS e campos UMA (`claims_redirect_uris`).

---

### Requisito 8: Microserviço `oidc-provider`

**User Story:** Como desenvolvedor de aplicações, quero que o servidor suporte
OpenID Connect 1.0, para que eu possa autenticar usuários e obter informações
de perfil de forma padronizada.

#### Critérios de Aceitação

1. WHEN um cliente enviar uma requisição ao endpoint `/userinfo` com um
   `AccessToken` válido contendo o escopo `openid`, THE OIDC_Provider SHALL
   retornar o `UserInfo` do usuário autenticado conforme OpenID Connect Core 1.0.
2. WHEN um `AccessToken` for emitido com escopo `openid`, THE
   Authorization_Server SHALL incluir um `id_token` JWT assinado na resposta
   do endpoint `/token`.
3. THE OIDC_Provider SHALL expor o endpoint `/.well-known/openid-configuration`
   com os metadados do servidor conforme OpenID Connect Discovery 1.0.
4. THE OIDC_Provider SHALL expor o endpoint `/jwks` com as chaves públicas JWK
   usadas para assinar ID Tokens.
5. WHEN um cliente com `subject_type=pairwise` solicitar um ID Token, THE
   OIDC_Provider SHALL gerar ou recuperar o `PairwiseIdentifier` correspondente
   ao par `(userSub, sectorIdentifier)` e usar o identificador pairwise como
   valor do claim `sub` no ID Token.
6. THE OIDC_Provider SHALL suportar assinatura de ID Tokens com algoritmos
   RS256, RS384, RS512, ES256, ES384, ES512 e PS256.
7. WHERE o cliente tiver configurado `userinfo_signed_response_alg`, THE
   OIDC_Provider SHALL retornar o UserInfo como JWT assinado com o algoritmo
   configurado.
8. WHERE o cliente tiver configurado `userinfo_encrypted_response_alg`, THE
   OIDC_Provider SHALL retornar o UserInfo como JWT criptografado com o
   algoritmo JWE configurado.
9. THE OIDC_Provider SHALL suportar o endpoint `/end_session` para logout
   iniciado pelo RP (RP-Initiated Logout).

---

### Requisito 9: Microserviço `uma-server`

**User Story:** Como Resource Owner, quero proteger meus recursos com UMA 2.0,
para que eu possa controlar quem acessa meus dados e sob quais condições.

#### Critérios de Aceitação

1. WHEN um Resource Server enviar uma requisição POST válida ao endpoint
   `/uma/resource_set`, THE UMA_Server SHALL criar um novo `ResourceSet`
   Aggregate e retornar o identificador do recurso com HTTP 201.
2. WHEN um Resource Server enviar uma requisição GET ao endpoint
   `/uma/resource_set/{id}`, THE UMA_Server SHALL retornar os metadados do
   `ResourceSet` com HTTP 200.
3. WHEN um Resource Server enviar uma requisição PUT ao endpoint
   `/uma/resource_set/{id}`, THE UMA_Server SHALL atualizar o `ResourceSet`
   Aggregate e retornar HTTP 200.
4. WHEN um Resource Server enviar uma requisição DELETE ao endpoint
   `/uma/resource_set/{id}`, THE UMA_Server SHALL remover o `ResourceSet`
   Aggregate e retornar HTTP 204.
5. WHEN um Resource Server enviar uma requisição POST ao endpoint
   `/uma/permission`, THE UMA_Server SHALL criar um `PermissionTicket`
   Aggregate associado ao `ResourceSet` e retornar o ticket com HTTP 201.
6. WHEN um Requesting Party apresentar um `PermissionTicket` válido e claims
   suficientes ao endpoint `/token` com `grant_type=urn:ietf:params:oauth:
   grant-type:uma-ticket`, THE UMA_Server SHALL emitir um Requesting Party
   Token (RPT) com as permissões aprovadas.
7. IF um `PermissionTicket` expirar antes de ser utilizado, THEN THE UMA_Server
   SHALL retornar HTTP 400 com `error=expired_ticket` ao tentar utilizá-lo.
8. THE UMA_Server SHALL avaliar as `Policy` associadas a um `ResourceSet` para
   determinar se os `Claim` fornecidos pelo Requesting Party satisfazem os
   requisitos de acesso.
9. WHEN um `ResourceSet` for deletado, THE UMA_Server SHALL invalidar todos os
   `PermissionTicket` e RPTs associados a esse recurso.

---

### Requisito 10: Microserviço `scope-manager`

**User Story:** Como administrador do sistema, quero gerenciar os escopos
disponíveis no servidor de autorização, para que eu possa controlar quais
permissões podem ser solicitadas pelos clientes.

#### Critérios de Aceitação

1. THE Scope_Manager SHALL manter um registro de todos os `SystemScope`
   Aggregates disponíveis no sistema.
2. WHEN um administrador criar um novo `SystemScope`, THE Scope_Manager SHALL
   persistir o escopo com os campos `value`, `description`, `icon`,
   `defaultScope` e `restricted`.
3. WHEN o Authorization_Server precisar validar os escopos de uma requisição,
   THE Authorization_Server SHALL consultar o Scope_Manager via API REST para
   verificar se os escopos solicitados existem e são permitidos para o cliente.
4. WHEN um cliente for registrado sem escopos explícitos, THE Client_Registry
   SHALL consultar o Scope_Manager para obter os `SystemScope` marcados como
   `defaultScope=true` e atribuí-los ao cliente.
5. IF um cliente solicitar um escopo marcado como `restricted=true`, THEN THE
   Authorization_Server SHALL rejeitar a requisição com HTTP 400 e
   `error=invalid_scope`, a menos que o cliente tenha sido explicitamente
   autorizado pelo administrador.

---

### Requisito 11: Serialização e Parsing de Tokens JWT

**User Story:** Como desenvolvedor, quero que os tokens JWT sejam serializados
e desserializados de forma confiável, para que tokens emitidos possam ser
validados corretamente em qualquer endpoint.

#### Critérios de Aceitação

1. WHEN o Token_Service emitir um `AccessToken`, THE Token_Service SHALL
   serializar o JWT usando a biblioteca Nimbus JOSE+JWT e armazenar o valor
   serializado como string no MongoDB.
2. WHEN o Token_Service receber um valor de token string, THE Token_Service
   SHALL fazer o parse do JWT usando a biblioteca Nimbus JOSE+JWT e retornar
   um objeto `AccessToken` ou `RefreshToken` válido.
3. THE Token_Service SHALL implementar um Pretty_Printer que formata objetos
   `AccessToken` e `RefreshToken` de volta para sua representação JWT string.
4. FOR ALL `AccessToken` válidos, serializar e depois fazer o parse do JWT
   SHALL produzir um objeto equivalente ao original (propriedade round-trip:
   `parse(serialize(token)).claims == token.claims`).
5. IF um valor de token string não for um JWT válido, THEN THE Token_Service
   SHALL retornar um erro de parsing tipado sem lançar exceção não tratada.
6. IF um JWT tiver assinatura inválida, THEN THE Token_Service SHALL rejeitar
   o token e retornar um erro de validação tipado.

---

### Requisito 12: Migração de Dados

**User Story:** Como operador do sistema, quero que os dados existentes no
banco relacional sejam migrados para o MongoDB, para que o sistema possa ser
colocado em produção sem perda de dados históricos.

#### Critérios de Aceitação

1. THE System SHALL fornecer um script ou ferramenta de migração que leia os
   dados das tabelas SQL existentes e os escreva nas coleções MongoDB
   correspondentes.
2. THE Migration_Tool SHALL migrar os dados preservando todos os campos
   mapeados nas entidades JPA originais, incluindo coleções de elementos
   (`@ElementCollection`) como subdocumentos ou arrays MongoDB.
3. WHEN a migração for executada, THE Migration_Tool SHALL registrar em log
   o número de documentos migrados por coleção e quaisquer erros encontrados.
4. IF um registro SQL não puder ser migrado por inconsistência de dados, THEN
   THE Migration_Tool SHALL registrar o registro problemático em um arquivo de
   relatório de erros e continuar a migração dos demais registros.
5. THE Migration_Tool SHALL ser idempotente: executar a migração múltiplas
   vezes SHALL produzir o mesmo resultado final no MongoDB, sem duplicação de
   documentos.

---

### Requisito 13: Observabilidade e Operação

**User Story:** Como operador do sistema, quero que cada microserviço exponha
métricas, logs estruturados e health checks, para que eu possa monitorar e
operar o sistema em produção.

#### Critérios de Aceitação

1. THE System SHALL expor um endpoint `/actuator/health` em cada microserviço
   que retorne o estado de saúde do serviço e de suas dependências (MongoDB,
   Event_Bus).
2. THE System SHALL expor métricas no formato Prometheus via endpoint
   `/actuator/metrics` em cada microserviço.
3. THE System SHALL emitir logs estruturados em formato JSON com os campos
   `timestamp`, `level`, `service`, `traceId`, `spanId` e `message` em cada
   microserviço.
4. WHEN uma requisição HTTP for recebida por qualquer microserviço, THE System
   SHALL propagar o `traceId` de distributed tracing (OpenTelemetry) através
   dos headers HTTP para correlação de logs entre serviços.
5. THE System SHALL expor um endpoint `/actuator/info` com a versão do serviço
   e informações de build em cada microserviço.
