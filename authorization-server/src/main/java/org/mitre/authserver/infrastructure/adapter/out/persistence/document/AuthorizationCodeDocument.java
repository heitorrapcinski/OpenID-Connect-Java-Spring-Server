package org.mitre.authserver.infrastructure.adapter.out.persistence.document;

import org.mitre.authserver.domain.model.AuthorizationCode;
import org.mitre.authserver.domain.model.vo.ClientId;
import org.mitre.authserver.domain.model.vo.CodeValue;
import org.mitre.authserver.domain.model.vo.PKCEChallenge;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("authorization_codes")
public class AuthorizationCodeDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code;

    private String clientId;

    @Indexed(expireAfterSeconds = 0)
    private Instant expiration;

    private boolean used;
    private String pkceChallenge;
    private String pkceChallengeMethod;
    private AuthenticationHolderDocument authenticationHolder;

    @Version
    private Long version;

    public AuthorizationCodeDocument() {}

    public static AuthorizationCodeDocument fromDomain(AuthorizationCode authCode) {
        AuthorizationCodeDocument doc = new AuthorizationCodeDocument();
        doc.id = authCode.getId();
        doc.code = authCode.getCodeValue().value();
        doc.clientId = authCode.getClientId() != null ? authCode.getClientId().value() : null;
        doc.expiration = authCode.getExpiration();
        doc.used = authCode.isUsed();
        if (authCode.getPkceChallenge() != null) {
            doc.pkceChallenge = authCode.getPkceChallenge().challenge();
            doc.pkceChallengeMethod = authCode.getPkceChallenge().method();
        }
        doc.authenticationHolder = AuthenticationHolderDocument.fromDomain(authCode.getAuthenticationHolder());
        return doc;
    }

    public AuthorizationCode toDomain() {
        PKCEChallenge pkce = null;
        if (pkceChallenge != null && pkceChallengeMethod != null) {
            pkce = new PKCEChallenge(pkceChallenge, pkceChallengeMethod);
        }
        return AuthorizationCode.reconstitute(
                id,
                new CodeValue(code),
                clientId != null ? new ClientId(clientId) : null,
                expiration,
                used,
                pkce,
                authenticationHolder != null ? authenticationHolder.toDomain() : null
        );
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public Instant getExpiration() { return expiration; }
    public void setExpiration(Instant expiration) { this.expiration = expiration; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
    public String getPkceChallenge() { return pkceChallenge; }
    public void setPkceChallenge(String pkceChallenge) { this.pkceChallenge = pkceChallenge; }
    public String getPkceChallengeMethod() { return pkceChallengeMethod; }
    public void setPkceChallengeMethod(String pkceChallengeMethod) { this.pkceChallengeMethod = pkceChallengeMethod; }
    public AuthenticationHolderDocument getAuthenticationHolder() { return authenticationHolder; }
    public void setAuthenticationHolder(AuthenticationHolderDocument authenticationHolder) { this.authenticationHolder = authenticationHolder; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
