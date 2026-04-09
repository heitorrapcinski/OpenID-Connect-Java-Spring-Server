package org.mitre.authserver.infrastructure.adapter.out.persistence.document;

import org.mitre.authserver.domain.model.RefreshToken;
import org.mitre.authserver.domain.model.vo.ClientId;
import org.mitre.authserver.domain.model.vo.Subject;
import org.mitre.authserver.domain.model.vo.TokenValue;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("refresh_tokens")
@CompoundIndexes({
    @CompoundIndex(name = "idx_client_id", def = "{'clientId': 1}"),
    @CompoundIndex(name = "idx_user_sub", def = "{'authenticationHolder.userSub': 1}")
})
public class RefreshTokenDocument {

    @Id
    private String id;

    @Indexed(expireAfterSeconds = 0)
    private Instant expiration;

    private String tokenValue;
    private String clientId;
    private boolean used;
    private AuthenticationHolderDocument authenticationHolder;

    @Version
    private Long version;

    public RefreshTokenDocument() {}

    public static RefreshTokenDocument fromDomain(RefreshToken token) {
        RefreshTokenDocument doc = new RefreshTokenDocument();
        doc.id = token.getId();
        doc.tokenValue = token.getTokenValue() != null ? token.getTokenValue().value() : null;
        doc.clientId = token.getClientId() != null ? token.getClientId().value() : null;
        doc.expiration = token.getExpiration();
        doc.used = token.isUsed();
        doc.authenticationHolder = AuthenticationHolderDocument.fromDomain(token.getAuthenticationHolder());
        return doc;
    }

    public RefreshToken toDomain() {
        String subStr = authenticationHolder != null ? authenticationHolder.getUserSub() : null;
        return RefreshToken.reconstitute(
                id,
                tokenValue != null ? new TokenValue(tokenValue) : null,
                clientId != null ? new ClientId(clientId) : null,
                subStr != null ? new Subject(subStr) : null,
                expiration,
                authenticationHolder != null ? authenticationHolder.toDomain() : null,
                used
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
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
    public AuthenticationHolderDocument getAuthenticationHolder() { return authenticationHolder; }
    public void setAuthenticationHolder(AuthenticationHolderDocument authenticationHolder) { this.authenticationHolder = authenticationHolder; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
