package org.mitre.authserver.domain.model;

import org.mitre.authserver.domain.exception.AuthorizationCodeReusedException;
import org.mitre.authserver.domain.model.vo.AuthenticationHolder;
import org.mitre.authserver.domain.model.vo.ClientId;
import org.mitre.authserver.domain.model.vo.CodeValue;
import org.mitre.authserver.domain.model.vo.PKCEChallenge;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate for authorization codes.
 * No framework annotations — pure domain object.
 */
public class AuthorizationCode {

    private final String id;
    private final CodeValue codeValue;
    private final ClientId clientId;
    private final Instant expiration;
    private boolean used;
    private final PKCEChallenge pkceChallenge; // nullable
    private final AuthenticationHolder authenticationHolder;

    private AuthorizationCode(String id, CodeValue codeValue, ClientId clientId,
                               Instant expiration, boolean used, PKCEChallenge pkceChallenge,
                               AuthenticationHolder authenticationHolder) {
        this.id = id;
        this.codeValue = codeValue;
        this.clientId = clientId;
        this.expiration = expiration;
        this.used = used;
        this.pkceChallenge = pkceChallenge;
        this.authenticationHolder = authenticationHolder;
    }

    public static AuthorizationCode create(String code, ClientId clientId,
                                           AuthenticationHolder authenticationHolder,
                                           Instant expiration, PKCEChallenge pkceChallenge) {
        return new AuthorizationCode(
                UUID.randomUUID().toString(),
                new CodeValue(code),
                clientId,
                expiration,
                false,
                pkceChallenge,
                authenticationHolder
        );
    }

    public static AuthorizationCode reconstitute(String id, CodeValue codeValue, ClientId clientId,
                                                  Instant expiration, boolean used,
                                                  PKCEChallenge pkceChallenge,
                                                  AuthenticationHolder authenticationHolder) {
        return new AuthorizationCode(id, codeValue, clientId, expiration, used, pkceChallenge, authenticationHolder);
    }

    public void consume() {
        if (used) {
            throw new AuthorizationCodeReusedException(
                    "Authorization code already used: " + codeValue.value());
        }
        this.used = true;
    }

    public boolean isExpired(Instant now) {
        return expiration.isBefore(now);
    }

    // Getters
    public String getId() { return id; }
    public CodeValue getCodeValue() { return codeValue; }
    public ClientId getClientId() { return clientId; }
    public Instant getExpiration() { return expiration; }
    public boolean isUsed() { return used; }
    public PKCEChallenge getPkceChallenge() { return pkceChallenge; }
    public AuthenticationHolder getAuthenticationHolder() { return authenticationHolder; }
}
