package org.mitre.clientregistry.domain.model;

import org.mitre.clientregistry.domain.exception.InvalidClientMetadataException;
import org.mitre.clientregistry.domain.model.vo.ClientId;
import org.mitre.clientregistry.domain.model.vo.ClientSecret;
import org.mitre.clientregistry.domain.model.vo.GrantType;
import org.mitre.clientregistry.domain.model.vo.JweAlgorithm;
import org.mitre.clientregistry.domain.model.vo.JwsAlgorithm;
import org.mitre.clientregistry.domain.model.vo.RedirectUri;
import org.mitre.clientregistry.domain.model.vo.ResponseType;
import org.mitre.clientregistry.domain.model.vo.SectorIdentifierUri;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Aggregate Root for the client-registry Bounded Context.
 * Represents an OAuth 2.0 / OIDC client registration.
 * No framework annotations — pure domain object.
 */
public class Client {

    private final ClientId clientId;
    private final ClientSecret clientSecret;
    private final String clientName;
    private final String clientDescription;
    private final String clientUri;
    private final String logoUri;
    private final String tosUri;
    private final String policyUri;
    private final Set<RedirectUri> redirectUris;
    private final Set<String> postLogoutRedirectUris;
    private final Set<String> contacts;
    private final Set<String> scope;
    private final Set<GrantType> grantTypes;
    private final Set<ResponseType> responseTypes;
    private final String tokenEndpointAuthMethod;
    private final String applicationType;
    private final String subjectType;
    private final SectorIdentifierUri sectorIdentifierUri;
    private final String jwksUri;
    private final String jwks;
    private final JwsAlgorithm idTokenSignedResponseAlg;
    private final JweAlgorithm idTokenEncryptedResponseAlg;
    private final String idTokenEncryptedResponseEnc;
    private final JwsAlgorithm userInfoSignedResponseAlg;
    private final JweAlgorithm userInfoEncryptedResponseAlg;
    private final String userInfoEncryptedResponseEnc;
    private final Integer accessTokenValiditySeconds;
    private final Integer refreshTokenValiditySeconds;
    private final Integer idTokenValiditySeconds;
    private final Integer deviceCodeValiditySeconds;
    private final boolean reuseRefreshToken;
    private final boolean clearAccessTokensOnRefresh;
    private final boolean dynamicallyRegistered;
    private final boolean allowIntrospection;
    private final String softwareId;
    private final String softwareVersion;
    private final String registrationAccessToken;
    private final Instant createdAt;

    private final List<Object> domainEvents = new ArrayList<>();

    private Client(Builder builder) {
        this.clientId = builder.clientId;
        this.clientSecret = builder.clientSecret;
        this.clientName = builder.clientName;
        this.clientDescription = builder.clientDescription;
        this.clientUri = builder.clientUri;
        this.logoUri = builder.logoUri;
        this.tosUri = builder.tosUri;
        this.policyUri = builder.policyUri;
        this.redirectUris = builder.redirectUris != null ? Collections.unmodifiableSet(builder.redirectUris) : Collections.emptySet();
        this.postLogoutRedirectUris = builder.postLogoutRedirectUris != null ? Collections.unmodifiableSet(builder.postLogoutRedirectUris) : Collections.emptySet();
        this.contacts = builder.contacts != null ? Collections.unmodifiableSet(builder.contacts) : Collections.emptySet();
        this.scope = builder.scope != null ? Collections.unmodifiableSet(builder.scope) : Collections.emptySet();
        this.grantTypes = builder.grantTypes != null ? Collections.unmodifiableSet(builder.grantTypes) : Collections.emptySet();
        this.responseTypes = builder.responseTypes != null ? Collections.unmodifiableSet(builder.responseTypes) : Collections.emptySet();
        this.tokenEndpointAuthMethod = builder.tokenEndpointAuthMethod;
        this.applicationType = builder.applicationType != null ? builder.applicationType : "WEB";
        this.subjectType = builder.subjectType != null ? builder.subjectType : "PUBLIC";
        this.sectorIdentifierUri = builder.sectorIdentifierUri;
        this.jwksUri = builder.jwksUri;
        this.jwks = builder.jwks;
        this.idTokenSignedResponseAlg = builder.idTokenSignedResponseAlg;
        this.idTokenEncryptedResponseAlg = builder.idTokenEncryptedResponseAlg;
        this.idTokenEncryptedResponseEnc = builder.idTokenEncryptedResponseEnc;
        this.userInfoSignedResponseAlg = builder.userInfoSignedResponseAlg;
        this.userInfoEncryptedResponseAlg = builder.userInfoEncryptedResponseAlg;
        this.userInfoEncryptedResponseEnc = builder.userInfoEncryptedResponseEnc;
        this.accessTokenValiditySeconds = builder.accessTokenValiditySeconds;
        this.refreshTokenValiditySeconds = builder.refreshTokenValiditySeconds;
        this.idTokenValiditySeconds = builder.idTokenValiditySeconds != null ? builder.idTokenValiditySeconds : 600;
        this.deviceCodeValiditySeconds = builder.deviceCodeValiditySeconds;
        this.reuseRefreshToken = builder.reuseRefreshToken;
        this.clearAccessTokensOnRefresh = builder.clearAccessTokensOnRefresh;
        this.dynamicallyRegistered = builder.dynamicallyRegistered;
        this.allowIntrospection = builder.allowIntrospection;
        this.softwareId = builder.softwareId;
        this.softwareVersion = builder.softwareVersion;
        this.registrationAccessToken = builder.registrationAccessToken;
        this.createdAt = builder.createdAt;
    }

    /**
     * Static factory — registers a new Client, enforcing domain invariants.
     */
    public static Client register(Builder builder) {
        // Invariant 1: idTokenValiditySeconds defaults to 600 if null (handled in constructor)
        // Invariant 2: grant_types / response_types compatibility
        validateGrantResponseTypeCompatibility(builder.grantTypes, builder.responseTypes);
        // Invariant 3: redirect_uris for WEB application type
        String appType = builder.applicationType != null ? builder.applicationType : "WEB";
        validateRedirectUris(builder.redirectUris, appType);

        return new Client(builder);
    }

    /**
     * Returns a new Client with updated fields (immutable update), re-validating invariants.
     */
    public Client update(Builder builder) {
        validateGrantResponseTypeCompatibility(builder.grantTypes, builder.responseTypes);
        String appType = builder.applicationType != null ? builder.applicationType : "WEB";
        validateRedirectUris(builder.redirectUris, appType);
        return new Client(builder);
    }

    private static void validateGrantResponseTypeCompatibility(Set<GrantType> grantTypes, Set<ResponseType> responseTypes) {
        if (responseTypes == null || grantTypes == null) return;

        boolean hasCodeResponseType = responseTypes.stream().anyMatch(rt -> "code".equals(rt.value()));
        boolean hasTokenResponseType = responseTypes.stream().anyMatch(rt -> "token".equals(rt.value()));
        boolean hasAuthorizationCodeGrant = grantTypes.stream().anyMatch(gt -> "authorization_code".equals(gt.value()));
        boolean hasImplicitGrant = grantTypes.stream().anyMatch(gt -> "implicit".equals(gt.value()));

        if (hasCodeResponseType && !hasAuthorizationCodeGrant) {
            throw new InvalidClientMetadataException(
                "response_type 'code' requires grant_type 'authorization_code'");
        }
        if (hasTokenResponseType && !hasImplicitGrant) {
            throw new InvalidClientMetadataException(
                "response_type 'token' requires grant_type 'implicit'");
        }
    }

    private static void validateRedirectUris(Set<RedirectUri> redirectUris, String applicationType) {
        if (redirectUris == null || !"WEB".equalsIgnoreCase(applicationType)) return;

        for (RedirectUri uri : redirectUris) {
            String v = uri.value();
            if (!v.startsWith("https://") && !v.startsWith("http://localhost")) {
                throw new InvalidClientMetadataException(
                    "WEB application redirect_uri must use https:// or http://localhost, got: " + v);
            }
        }
    }

    /** Returns and clears accumulated domain events. */
    public List<Object> pullDomainEvents() {
        List<Object> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }

    public void registerEvent(Object event) {
        domainEvents.add(event);
    }

    // --- Getters ---

    public ClientId getClientId() { return clientId; }
    public ClientSecret getClientSecret() { return clientSecret; }
    public String getClientName() { return clientName; }
    public String getClientDescription() { return clientDescription; }
    public String getClientUri() { return clientUri; }
    public String getLogoUri() { return logoUri; }
    public String getTosUri() { return tosUri; }
    public String getPolicyUri() { return policyUri; }
    public Set<RedirectUri> getRedirectUris() { return redirectUris; }
    public Set<String> getPostLogoutRedirectUris() { return postLogoutRedirectUris; }
    public Set<String> getContacts() { return contacts; }
    public Set<String> getScope() { return scope; }
    public Set<GrantType> getGrantTypes() { return grantTypes; }
    public Set<ResponseType> getResponseTypes() { return responseTypes; }
    public String getTokenEndpointAuthMethod() { return tokenEndpointAuthMethod; }
    public String getApplicationType() { return applicationType; }
    public String getSubjectType() { return subjectType; }
    public SectorIdentifierUri getSectorIdentifierUri() { return sectorIdentifierUri; }
    public String getJwksUri() { return jwksUri; }
    public String getJwks() { return jwks; }
    public JwsAlgorithm getIdTokenSignedResponseAlg() { return idTokenSignedResponseAlg; }
    public JweAlgorithm getIdTokenEncryptedResponseAlg() { return idTokenEncryptedResponseAlg; }
    public String getIdTokenEncryptedResponseEnc() { return idTokenEncryptedResponseEnc; }
    public JwsAlgorithm getUserInfoSignedResponseAlg() { return userInfoSignedResponseAlg; }
    public JweAlgorithm getUserInfoEncryptedResponseAlg() { return userInfoEncryptedResponseAlg; }
    public String getUserInfoEncryptedResponseEnc() { return userInfoEncryptedResponseEnc; }
    public Integer getAccessTokenValiditySeconds() { return accessTokenValiditySeconds; }
    public Integer getRefreshTokenValiditySeconds() { return refreshTokenValiditySeconds; }
    public Integer getIdTokenValiditySeconds() { return idTokenValiditySeconds; }
    public Integer getDeviceCodeValiditySeconds() { return deviceCodeValiditySeconds; }
    public boolean isReuseRefreshToken() { return reuseRefreshToken; }
    public boolean isClearAccessTokensOnRefresh() { return clearAccessTokensOnRefresh; }
    public boolean isDynamicallyRegistered() { return dynamicallyRegistered; }
    public boolean isAllowIntrospection() { return allowIntrospection; }
    public String getSoftwareId() { return softwareId; }
    public String getSoftwareVersion() { return softwareVersion; }
    public String getRegistrationAccessToken() { return registrationAccessToken; }
    public Instant getCreatedAt() { return createdAt; }

    // --- Builder ---

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ClientId clientId;
        private ClientSecret clientSecret;
        private String clientName;
        private String clientDescription;
        private String clientUri;
        private String logoUri;
        private String tosUri;
        private String policyUri;
        private Set<RedirectUri> redirectUris;
        private Set<String> postLogoutRedirectUris;
        private Set<String> contacts;
        private Set<String> scope;
        private Set<GrantType> grantTypes;
        private Set<ResponseType> responseTypes;
        private String tokenEndpointAuthMethod;
        private String applicationType;
        private String subjectType;
        private SectorIdentifierUri sectorIdentifierUri;
        private String jwksUri;
        private String jwks;
        private JwsAlgorithm idTokenSignedResponseAlg;
        private JweAlgorithm idTokenEncryptedResponseAlg;
        private String idTokenEncryptedResponseEnc;
        private JwsAlgorithm userInfoSignedResponseAlg;
        private JweAlgorithm userInfoEncryptedResponseAlg;
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

        public Builder clientId(ClientId clientId) { this.clientId = clientId; return this; }
        public Builder clientSecret(ClientSecret clientSecret) { this.clientSecret = clientSecret; return this; }
        public Builder clientName(String clientName) { this.clientName = clientName; return this; }
        public Builder clientDescription(String clientDescription) { this.clientDescription = clientDescription; return this; }
        public Builder clientUri(String clientUri) { this.clientUri = clientUri; return this; }
        public Builder logoUri(String logoUri) { this.logoUri = logoUri; return this; }
        public Builder tosUri(String tosUri) { this.tosUri = tosUri; return this; }
        public Builder policyUri(String policyUri) { this.policyUri = policyUri; return this; }
        public Builder redirectUris(Set<RedirectUri> redirectUris) { this.redirectUris = redirectUris; return this; }
        public Builder postLogoutRedirectUris(Set<String> postLogoutRedirectUris) { this.postLogoutRedirectUris = postLogoutRedirectUris; return this; }
        public Builder contacts(Set<String> contacts) { this.contacts = contacts; return this; }
        public Builder scope(Set<String> scope) { this.scope = scope; return this; }
        public Builder grantTypes(Set<GrantType> grantTypes) { this.grantTypes = grantTypes; return this; }
        public Builder responseTypes(Set<ResponseType> responseTypes) { this.responseTypes = responseTypes; return this; }
        public Builder tokenEndpointAuthMethod(String tokenEndpointAuthMethod) { this.tokenEndpointAuthMethod = tokenEndpointAuthMethod; return this; }
        public Builder applicationType(String applicationType) { this.applicationType = applicationType; return this; }
        public Builder subjectType(String subjectType) { this.subjectType = subjectType; return this; }
        public Builder sectorIdentifierUri(SectorIdentifierUri sectorIdentifierUri) { this.sectorIdentifierUri = sectorIdentifierUri; return this; }
        public Builder jwksUri(String jwksUri) { this.jwksUri = jwksUri; return this; }
        public Builder jwks(String jwks) { this.jwks = jwks; return this; }
        public Builder idTokenSignedResponseAlg(JwsAlgorithm idTokenSignedResponseAlg) { this.idTokenSignedResponseAlg = idTokenSignedResponseAlg; return this; }
        public Builder idTokenEncryptedResponseAlg(JweAlgorithm idTokenEncryptedResponseAlg) { this.idTokenEncryptedResponseAlg = idTokenEncryptedResponseAlg; return this; }
        public Builder idTokenEncryptedResponseEnc(String idTokenEncryptedResponseEnc) { this.idTokenEncryptedResponseEnc = idTokenEncryptedResponseEnc; return this; }
        public Builder userInfoSignedResponseAlg(JwsAlgorithm userInfoSignedResponseAlg) { this.userInfoSignedResponseAlg = userInfoSignedResponseAlg; return this; }
        public Builder userInfoEncryptedResponseAlg(JweAlgorithm userInfoEncryptedResponseAlg) { this.userInfoEncryptedResponseAlg = userInfoEncryptedResponseAlg; return this; }
        public Builder userInfoEncryptedResponseEnc(String userInfoEncryptedResponseEnc) { this.userInfoEncryptedResponseEnc = userInfoEncryptedResponseEnc; return this; }
        public Builder accessTokenValiditySeconds(Integer accessTokenValiditySeconds) { this.accessTokenValiditySeconds = accessTokenValiditySeconds; return this; }
        public Builder refreshTokenValiditySeconds(Integer refreshTokenValiditySeconds) { this.refreshTokenValiditySeconds = refreshTokenValiditySeconds; return this; }
        public Builder idTokenValiditySeconds(Integer idTokenValiditySeconds) { this.idTokenValiditySeconds = idTokenValiditySeconds; return this; }
        public Builder deviceCodeValiditySeconds(Integer deviceCodeValiditySeconds) { this.deviceCodeValiditySeconds = deviceCodeValiditySeconds; return this; }
        public Builder reuseRefreshToken(boolean reuseRefreshToken) { this.reuseRefreshToken = reuseRefreshToken; return this; }
        public Builder clearAccessTokensOnRefresh(boolean clearAccessTokensOnRefresh) { this.clearAccessTokensOnRefresh = clearAccessTokensOnRefresh; return this; }
        public Builder dynamicallyRegistered(boolean dynamicallyRegistered) { this.dynamicallyRegistered = dynamicallyRegistered; return this; }
        public Builder allowIntrospection(boolean allowIntrospection) { this.allowIntrospection = allowIntrospection; return this; }
        public Builder softwareId(String softwareId) { this.softwareId = softwareId; return this; }
        public Builder softwareVersion(String softwareVersion) { this.softwareVersion = softwareVersion; return this; }
        public Builder registrationAccessToken(String registrationAccessToken) { this.registrationAccessToken = registrationAccessToken; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public Client build() {
            return new Client(this);
        }
    }
}
