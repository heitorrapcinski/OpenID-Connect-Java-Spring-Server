package org.mitre.clientregistry.infrastructure.adapter.out.persistence;

import org.mitre.clientregistry.domain.model.Client;
import org.mitre.clientregistry.domain.model.vo.ClientId;
import org.mitre.clientregistry.domain.model.vo.ClientSecret;
import org.mitre.clientregistry.domain.model.vo.GrantType;
import org.mitre.clientregistry.domain.model.vo.JweAlgorithm;
import org.mitre.clientregistry.domain.model.vo.JwsAlgorithm;
import org.mitre.clientregistry.domain.model.vo.RedirectUri;
import org.mitre.clientregistry.domain.model.vo.ResponseType;
import org.mitre.clientregistry.domain.model.vo.SectorIdentifierUri;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Document("clients")
public class ClientDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String clientId;

    @Version
    private Long version;

    private String clientSecret;
    private String clientName;
    private String clientDescription;
    private String clientUri;
    private String logoUri;
    private String tosUri;
    private String policyUri;
    private Set<String> redirectUris;
    private Set<String> postLogoutRedirectUris;
    private Set<String> contacts;
    private Set<String> scope;
    private Set<String> grantTypes;
    private Set<String> responseTypes;
    private String tokenEndpointAuthMethod;
    private String applicationType;
    private String subjectType;
    private String sectorIdentifierUri;
    private String jwksUri;
    private String jwks;
    private String idTokenSignedResponseAlg;
    private String idTokenEncryptedResponseAlg;
    private String idTokenEncryptedResponseEnc;
    private String userInfoSignedResponseAlg;
    private String userInfoEncryptedResponseAlg;
    private String userInfoEncryptedResponseEnc;
    private Integer accessTokenValiditySeconds;
    private Integer refreshTokenValiditySeconds;
    private Integer idTokenValiditySeconds;
    private Integer deviceCodeValiditySeconds;
    private boolean reuseRefreshToken;
    private boolean clearAccessTokensOnRefresh;
    private boolean dynamicallyRegistered;
    private boolean allowIntrospection;
    private String softwareId;
    private String softwareVersion;
    private String registrationAccessToken;
    private Instant createdAt;

    public ClientDocument() {}

    public static ClientDocument fromDomain(Client client) {
        ClientDocument doc = new ClientDocument();
        doc.id = client.getClientId().value();
        doc.clientId = client.getClientId().value();
        doc.clientSecret = client.getClientSecret().value();
        doc.clientName = client.getClientName();
        doc.clientDescription = client.getClientDescription();
        doc.clientUri = client.getClientUri();
        doc.logoUri = client.getLogoUri();
        doc.tosUri = client.getTosUri();
        doc.policyUri = client.getPolicyUri();
        doc.redirectUris = client.getRedirectUris() != null
                ? client.getRedirectUris().stream().map(RedirectUri::value).collect(Collectors.toSet())
                : null;
        doc.postLogoutRedirectUris = client.getPostLogoutRedirectUris();
        doc.contacts = client.getContacts();
        doc.scope = client.getScope();
        doc.grantTypes = client.getGrantTypes() != null
                ? client.getGrantTypes().stream().map(GrantType::value).collect(Collectors.toSet())
                : null;
        doc.responseTypes = client.getResponseTypes() != null
                ? client.getResponseTypes().stream().map(ResponseType::value).collect(Collectors.toSet())
                : null;
        doc.tokenEndpointAuthMethod = client.getTokenEndpointAuthMethod();
        doc.applicationType = client.getApplicationType();
        doc.subjectType = client.getSubjectType();
        doc.sectorIdentifierUri = client.getSectorIdentifierUri() != null
                ? client.getSectorIdentifierUri().value() : null;
        doc.jwksUri = client.getJwksUri();
        doc.jwks = client.getJwks();
        doc.idTokenSignedResponseAlg = client.getIdTokenSignedResponseAlg() != null
                ? client.getIdTokenSignedResponseAlg().value() : null;
        doc.idTokenEncryptedResponseAlg = client.getIdTokenEncryptedResponseAlg() != null
                ? client.getIdTokenEncryptedResponseAlg().value() : null;
        doc.idTokenEncryptedResponseEnc = client.getIdTokenEncryptedResponseEnc();
        doc.userInfoSignedResponseAlg = client.getUserInfoSignedResponseAlg() != null
                ? client.getUserInfoSignedResponseAlg().value() : null;
        doc.userInfoEncryptedResponseAlg = client.getUserInfoEncryptedResponseAlg() != null
                ? client.getUserInfoEncryptedResponseAlg().value() : null;
        doc.userInfoEncryptedResponseEnc = client.getUserInfoEncryptedResponseEnc();
        doc.accessTokenValiditySeconds = client.getAccessTokenValiditySeconds();
        doc.refreshTokenValiditySeconds = client.getRefreshTokenValiditySeconds();
        doc.idTokenValiditySeconds = client.getIdTokenValiditySeconds();
        doc.deviceCodeValiditySeconds = client.getDeviceCodeValiditySeconds();
        doc.reuseRefreshToken = client.isReuseRefreshToken();
        doc.clearAccessTokensOnRefresh = client.isClearAccessTokensOnRefresh();
        doc.dynamicallyRegistered = client.isDynamicallyRegistered();
        doc.allowIntrospection = client.isAllowIntrospection();
        doc.softwareId = client.getSoftwareId();
        doc.softwareVersion = client.getSoftwareVersion();
        doc.registrationAccessToken = client.getRegistrationAccessToken();
        doc.createdAt = client.getCreatedAt();
        return doc;
    }

    public Client toDomain() {
        return Client.builder()
                .clientId(new ClientId(clientId))
                .clientSecret(new ClientSecret(clientSecret))
                .clientName(clientName)
                .clientDescription(clientDescription)
                .clientUri(clientUri)
                .logoUri(logoUri)
                .tosUri(tosUri)
                .policyUri(policyUri)
                .redirectUris(redirectUris != null
                        ? redirectUris.stream().map(RedirectUri::new).collect(Collectors.toSet())
                        : null)
                .postLogoutRedirectUris(postLogoutRedirectUris)
                .contacts(contacts)
                .scope(scope)
                .grantTypes(grantTypes != null
                        ? grantTypes.stream().map(GrantType::new).collect(Collectors.toSet())
                        : null)
                .responseTypes(responseTypes != null
                        ? responseTypes.stream().map(ResponseType::new).collect(Collectors.toSet())
                        : null)
                .tokenEndpointAuthMethod(tokenEndpointAuthMethod)
                .applicationType(applicationType)
                .subjectType(subjectType)
                .sectorIdentifierUri(sectorIdentifierUri != null ? new SectorIdentifierUri(sectorIdentifierUri) : null)
                .jwksUri(jwksUri)
                .jwks(jwks)
                .idTokenSignedResponseAlg(idTokenSignedResponseAlg != null ? new JwsAlgorithm(idTokenSignedResponseAlg) : null)
                .idTokenEncryptedResponseAlg(idTokenEncryptedResponseAlg != null ? new JweAlgorithm(idTokenEncryptedResponseAlg) : null)
                .idTokenEncryptedResponseEnc(idTokenEncryptedResponseEnc)
                .userInfoSignedResponseAlg(userInfoSignedResponseAlg != null ? new JwsAlgorithm(userInfoSignedResponseAlg) : null)
                .userInfoEncryptedResponseAlg(userInfoEncryptedResponseAlg != null ? new JweAlgorithm(userInfoEncryptedResponseAlg) : null)
                .userInfoEncryptedResponseEnc(userInfoEncryptedResponseEnc)
                .accessTokenValiditySeconds(accessTokenValiditySeconds)
                .refreshTokenValiditySeconds(refreshTokenValiditySeconds)
                .idTokenValiditySeconds(idTokenValiditySeconds)
                .deviceCodeValiditySeconds(deviceCodeValiditySeconds)
                .reuseRefreshToken(reuseRefreshToken)
                .clearAccessTokensOnRefresh(clearAccessTokensOnRefresh)
                .dynamicallyRegistered(dynamicallyRegistered)
                .allowIntrospection(allowIntrospection)
                .softwareId(softwareId)
                .softwareVersion(softwareVersion)
                .registrationAccessToken(registrationAccessToken)
                .createdAt(createdAt)
                .build();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getRegistrationAccessToken() { return registrationAccessToken; }
    public void setRegistrationAccessToken(String registrationAccessToken) { this.registrationAccessToken = registrationAccessToken; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
