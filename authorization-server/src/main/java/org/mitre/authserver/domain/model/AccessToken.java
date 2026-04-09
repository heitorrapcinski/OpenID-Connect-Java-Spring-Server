package org.mitre.authserver.domain.model;

import org.mitre.authserver.domain.event.AccessTokenIssued;
import org.mitre.authserver.domain.event.AccessTokenRevoked;
import org.mitre.authserver.domain.model.vo.AuthenticationHolder;
import org.mitre.authserver.domain.model.vo.ClientId;
import org.mitre.authserver.domain.model.vo.Scope;
import org.mitre.authserver.domain.model.vo.Subject;
import org.mitre.authserver.domain.model.vo.TokenValue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate Root for access tokens in the authorization-server Bounded Context.
 * No framework annotations — pure domain object.
 */
public class AccessToken {

    public record Permission(String resourceSetId, List<String> scopes) {}

    private final String id;
    private TokenValue tokenValue;
    private final ClientId clientId;
    private final Subject subject;
    private final Scope scope;
    private final Instant expiration;
    private final String tokenType;
    private final String refreshTokenId;
    private final String approvedSiteId;
    private final AuthenticationHolder authenticationHolder;
    private final List<Permission> permissions;

    private final List<Object> domainEvents = new ArrayList<>();

    private AccessToken(String id, TokenValue tokenValue, ClientId clientId, Subject subject,
                        Scope scope, Instant expiration, String tokenType, String refreshTokenId,
                        String approvedSiteId, AuthenticationHolder authenticationHolder,
                        List<Permission> permissions) {
        this.id = id;
        this.tokenValue = tokenValue;
        this.clientId = clientId;
        this.subject = subject;
        this.scope = scope;
        this.expiration = expiration;
        this.tokenType = tokenType != null ? tokenType : "Bearer";
        this.refreshTokenId = refreshTokenId;
        this.approvedSiteId = approvedSiteId;
        this.authenticationHolder = authenticationHolder;
        this.permissions = permissions != null ? Collections.unmodifiableList(permissions) : Collections.emptyList();
    }

    /**
     * Static factory — issues a new AccessToken, enforcing domain invariants.
     */
    public static AccessToken issue(ClientId clientId, Subject subject, Scope scope,
                                    Instant expiration, AuthenticationHolder authenticationHolder,
                                    String id) {
        if (expiration == null) {
            throw new IllegalArgumentException("AccessToken expiration must not be null");
        }
        AccessToken token = new AccessToken(
                id != null ? id : UUID.randomUUID().toString(),
                null, // tokenValue set after JWT serialization
                clientId, subject, scope, expiration,
                "Bearer", null, null, authenticationHolder, null
        );
        token.registerEvent(new AccessTokenIssued(
                UUID.randomUUID().toString(),
                "AccessTokenIssued",
                token.id,
                Instant.now(),
                "authorization-server",
                null,
                new AccessTokenIssued.AccessTokenIssuedPayload(
                        token.id,
                        clientId.value(),
                        subject != null ? subject.value() : null,
                        scope.values()
                )
        ));
        return token;
    }

    /**
     * Reconstruct from persistence (no events).
     */
    public static AccessToken reconstitute(String id, TokenValue tokenValue, ClientId clientId,
                                           Subject subject, Scope scope, Instant expiration,
                                           String tokenType, String refreshTokenId,
                                           String approvedSiteId, AuthenticationHolder authenticationHolder,
                                           List<Permission> permissions) {
        return new AccessToken(id, tokenValue, clientId, subject, scope, expiration,
                tokenType, refreshTokenId, approvedSiteId, authenticationHolder, permissions);
    }

    public boolean isExpired(Instant now) {
        return expiration.isBefore(now);
    }

    public AccessTokenRevoked revoke() {
        AccessTokenRevoked event = new AccessTokenRevoked(
                UUID.randomUUID().toString(),
                "AccessTokenRevoked",
                this.id,
                Instant.now(),
                "authorization-server",
                null,
                new AccessTokenRevoked.AccessTokenRevokedPayload(this.id, clientId.value())
        );
        registerEvent(event);
        return event;
    }

    public void setTokenValue(TokenValue tokenValue) {
        this.tokenValue = tokenValue;
    }

    public List<Object> pullDomainEvents() {
        List<Object> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }

    public void registerEvent(Object event) {
        domainEvents.add(event);
    }

    // Getters
    public String getId() { return id; }
    public TokenValue getTokenValue() { return tokenValue; }
    public ClientId getClientId() { return clientId; }
    public Subject getSubject() { return subject; }
    public Scope getScope() { return scope; }
    public Instant getExpiration() { return expiration; }
    public String getTokenType() { return tokenType; }
    public String getRefreshTokenId() { return refreshTokenId; }
    public String getApprovedSiteId() { return approvedSiteId; }
    public AuthenticationHolder getAuthenticationHolder() { return authenticationHolder; }
    public List<Permission> getPermissions() { return permissions; }
}
