package org.mitre.authserver.application.service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.mitre.authserver.domain.model.AccessToken;
import org.mitre.authserver.domain.model.vo.ClientId;
import org.mitre.authserver.domain.model.vo.Scope;
import org.mitre.authserver.domain.model.vo.Subject;
import org.mitre.authserver.domain.model.vo.TokenValue;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Component
public class TokenParser {

    private final RSAKey rsaPublicKey;

    public TokenParser(RSAKey rsaKey) {
        this.rsaPublicKey = rsaKey.toPublicJWK();
    }

    public sealed interface ParseResult permits ParseResult.Success, ParseResult.Failure {
        record Success(AccessToken token) implements ParseResult {}
        record Failure(String errorMessage) implements ParseResult {}
    }

    public ParseResult parse(String tokenString) {
        if (tokenString == null || tokenString.isBlank()) {
            return new ParseResult.Failure("Token string must not be blank");
        }

        SignedJWT signedJWT;
        try {
            signedJWT = SignedJWT.parse(tokenString);
        } catch (Exception e) {
            return new ParseResult.Failure("Invalid JWT format: " + e.getMessage());
        }

        // Reject algorithm=none
        if (JWSAlgorithm.NONE.equals(signedJWT.getHeader().getAlgorithm())) {
            return new ParseResult.Failure("Algorithm 'none' is not allowed");
        }

        // Validate RS256 signature
        try {
            RSASSAVerifier verifier = new RSASSAVerifier(rsaPublicKey.toRSAPublicKey());
            if (!signedJWT.verify(verifier)) {
                return new ParseResult.Failure("Invalid JWT signature");
            }
        } catch (Exception e) {
            return new ParseResult.Failure("Signature verification failed: " + e.getMessage());
        }

        JWTClaimsSet claims;
        try {
            claims = signedJWT.getJWTClaimsSet();
        } catch (Exception e) {
            return new ParseResult.Failure("Failed to parse JWT claims: " + e.getMessage());
        }

        // Check expiration
        Date expDate = claims.getExpirationTime();
        if (expDate == null || expDate.before(new Date())) {
            return new ParseResult.Failure("Token is expired");
        }

        try {
            String id = claims.getJWTID();
            String clientIdStr = (String) claims.getClaim("client_id");
            String subStr = claims.getSubject();
            String scopeStr = (String) claims.getClaim("scope");

            Set<String> scopeValues = new HashSet<>();
            if (scopeStr != null && !scopeStr.isBlank()) {
                scopeValues.addAll(Arrays.asList(scopeStr.split("\\s+")));
            }

            AccessToken token = AccessToken.reconstitute(
                    id,
                    new TokenValue(tokenString),
                    clientIdStr != null ? new ClientId(clientIdStr) : null,
                    subStr != null ? new Subject(subStr) : null,
                    Scope.of(scopeValues),
                    expDate.toInstant(),
                    "Bearer",
                    null, null, null, null
            );
            return new ParseResult.Success(token);
        } catch (Exception e) {
            return new ParseResult.Failure("Failed to reconstruct token: " + e.getMessage());
        }
    }
}
