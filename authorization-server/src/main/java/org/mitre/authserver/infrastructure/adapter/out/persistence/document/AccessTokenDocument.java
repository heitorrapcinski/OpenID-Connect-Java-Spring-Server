package org.mitre.authserver.infrastructure.adapter.out.persistence.document;

import org.mitre.authserver.domain.model.AccessToken;
import org.mitre.authserver.domain.model.vo.ClientId;
import org.mitre.authserver.domain.model.vo.Scope;
import org.mitre.authserver.domain.model.vo.Subject;
import org.mitre.authserver.domain.model.vo.TokenValue;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Document("access_tokens")
@CompoundIndexes({
    @CompoundIndex(name = "idx_client_id", def = "{'clientId': 1}"),
    @CompoundIndex(name = "idx_user_sub", def = "{'authenticationHolder.userSub': 1}"),
    @CompoundIndex(name = "idx_refresh_token_id", def = "{'refreshTokenId': 1}"),
    @CompoundIndex(name = "idx_approved_site_id", def = "{'approvedSiteId': 1}")
})
public class AccessTokenDocument {

    public static class PermissionDocument {
        private String resourceSetId;
        private List<String> scopes;

        public PermissionDocument() {}

        public PermissionDocument(String resourceSetId, List<String> scopes) {
            this.resourceSetId = resourceSetId;
            this.scopes = scopes;
        }

        public String getResourceSetId() { return resourceSetId; }
        public void setResourceSetId(String resourceSetId) { this.resourceSetId = resourceSetId; }
        public List<String> getScopes() { return scopes; }
        public void setScopes(List<String> scopes) { this.scopes = scopes; }
    }

    @Id
    private String id;

    @Indexed(expireAfterSeconds = 0)
    private Instant expiration;

    private String tokenValue;
    private String clientId;
    private String tokenType;
    private Set<String> scope;
    private String refreshTokenId;
    private String approvedSiteId;
    private AuthenticationHolderDocument authenticationHolder;
    private List<PermissionDocument> permissions;

    @Version
    private Long version;

    public AccessTokenDocument() {}

    public static AccessTokenDocument fromDomain(AccessToken token) {
        AccessTokenDocument doc = new AccessTokenDocument();
        doc.id = token.getId();
        doc.tokenValue = token.getTokenValue() != null ? token.getTokenValue().value() : null;
        doc.clientId = token.getClientId() != null ? token.getClientId().value() : null;
        doc.expiration = token.getExpiration();
        doc.tokenType = token.getTokenType();
        doc.scope = token.getScope().values();
        doc.refreshTokenId = token.getRefreshTokenId();
        doc.approvedSiteId = token.getApprovedSiteId();
        doc.authenticationHolder = AuthenticationHolderDocument.fromDomain(token.getAuthenticationHolder());
        if (token.getPermissions() != null) {
            doc.permissions = token.getPermissions().stream()
                    .map(p -> new PermissionDocument(p.resourceSetId(), p.scopes()))
                    .collect(Collectors.toList());
        }
        return doc;
    }

    public AccessToken toDomain() {
        List<AccessToken.Permission> domainPermissions = null;
        if (permissions != null) {
            domainPermissions = permissions.stream()
                    .map(p -> new AccessToken.Permission(p.getResourceSetId(), p.getScopes()))
                    .collect(Collectors.toList());
        }
        return AccessToken.reconstitute(
                id,
                tokenValue != null ? new TokenValue(tokenValue) : null,
                clientId != null ? new ClientId(clientId) : null,
                null, // subject stored in authHolder
                Scope.of(scope != null ? scope : Set.of()),
                expiration,
                tokenType,
                refreshTokenId,
                approvedSiteId,
                authenticationHolder != null ? authenticationHolder.toDomain() : null,
                domainPermissions
        );
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTokenValue() { return tokenValue; }
    public void setTokenValue(String tokenValue) { this.tokenValue = tokenValue; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public Instant getExpiration() { return expiration; }
    public void setExpiration(Instant expiration) { this.expiration = expiration; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public Set<String> getScope() { return scope; }
    public void setScope(Set<String> scope) { this.scope = scope; }
    public String getRefreshTokenId() { return refreshTokenId; }
    public void setRefreshTokenId(String refreshTokenId) { this.refreshTokenId = refreshTokenId; }
    public String getApprovedSiteId() { return approvedSiteId; }
    public void setApprovedSiteId(String approvedSiteId) { this.approvedSiteId = approvedSiteId; }
    public AuthenticationHolderDocument getAuthenticationHolder() { return authenticationHolder; }
    public void setAuthenticationHolder(AuthenticationHolderDocument authenticationHolder) { this.authenticationHolder = authenticationHolder; }
    public List<PermissionDocument> getPermissions() { return permissions; }
    public void setPermissions(List<PermissionDocument> permissions) { this.permissions = permissions; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
