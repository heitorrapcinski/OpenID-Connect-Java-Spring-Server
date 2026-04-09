package org.mitre.authserver.domain.model;

import org.mitre.authserver.domain.event.RefreshTokenRevoked;
import org.mitre.authserver.domain.exception.InvalidGrantException;
import org.mitre.authserver.domain.model.vo.AuthenticationHolder;
import org.mitre.authserver.domain.model.vo.ClientId;
import org.mitre.authserver.domain.model.vo.Subject;
import org.mitre.authserver.domain.model.vo.TokenValue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate Root for refresh tokens.
 * No framework annotations — pure domain object.
 */
public class RefreshToken {

    private final String id;
    private TokenValue tokenValue;
    private final ClientId clientId;
    private final Subject subject;
    private final Instant expiration;
    private final AuthenticationHolder authenticationHolder;
    private boolean used;

    private final List<Object> domainEvents = new ArrayList<>();

    private RefreshToken(String id, TokenValue tokenValue, ClientId clientId, Subject subject,
                         Instant expiration, AuthenticationHolder authenticationHolder, boolean used) {
        this.id = id;
        this.tokenValue = tokenValue;
        this.clientId = clientId;
        this.subject = subject;
        this.expiration = expiration;
        this.authenticationHolder = authenticationHolder;
        this.used = used;
    }

    public static RefreshToken issue(ClientId clientId, Subject subject, Instant expiration,
                                     AuthenticationHolder authenticationHolder, String id) {
        if (expiration == null) {
            throw new IllegalArgumentException("RefreshToken expiration must not be null");
        }
        return new RefreshToken(
                id != null ? id : UUID.randomUUID().toString(),
                null,
                clientId, subject, expiration, authenticationHolder, false
        );
    }

    public static RefreshToken reconstitute(String id, TokenValue tokenValue, ClientId clientId,
                                            Subject subject, Instant expiration,
                                            AuthenticationHolder authenticationHolder, boolean used) {
        return new RefreshToken(id, tokenValue, clientId, subject, expiration, authenticationHolder, used);
    }

    public void use() {
        if (used) {
            throw new InvalidGrantException("Refresh token already used");
        }
        this.used = true;
        registerEvent(new RefreshTokenRevoked(
                UUID.randomUUID().toString(),
                "RefreshTokenRevoked",
                this.id,
                Instant.now(),
                "authorization-server",
                null,
                new RefreshTokenRevoked.RefreshTokenRevokedPayload(this.id, clientId.value())
        ));
    }

    public boolean isExpired(Instant now) {
        return expiration.isBefore(now);
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
    public Instant getExpiration() { return expiration; }
    public AuthenticationHolder getAuthenticationHolder() { return authenticationHolder; }
    public boolean isUsed() { return used; }
}
