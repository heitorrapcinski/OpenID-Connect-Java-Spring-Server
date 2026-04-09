package org.mitre.authserver.application.service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.mitre.authserver.domain.model.AccessToken;
import org.mitre.authserver.domain.model.RefreshToken;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.stream.Collectors;

@Component
public class TokenSerializer {

    private final RSAKey rsaKey;

    public TokenSerializer(RSAKey rsaKey) {
        this.rsaKey = rsaKey;
    }

    public String serialize(AccessToken token) {
        try {
            String scopeStr = token.getScope().values().stream()
                    .sorted()
                    .collect(Collectors.joining(" "));

            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .jwtID(token.getId())
                    .issueTime(new Date())
                    .expirationTime(Date.from(token.getExpiration()))
                    .claim("client_id", token.getClientId().value())
                    .claim("scope", scopeStr)
                    .claim("token_type", token.getTokenType());

            if (token.getSubject() != null) {
                claimsBuilder.subject(token.getSubject().value());
            }

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(rsaKey.getKeyID())
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claimsBuilder.build());
            signedJWT.sign(new RSASSASigner(rsaKey));
            return signedJWT.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize AccessToken to JWT", e);
        }
    }

    public String serialize(RefreshToken token) {
        try {
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .jwtID(token.getId())
                    .issueTime(new Date())
                    .expirationTime(Date.from(token.getExpiration()))
                    .claim("client_id", token.getClientId().value())
                    .claim("token_type", "refresh_token");

            if (token.getSubject() != null) {
                claimsBuilder.subject(token.getSubject().value());
            }

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(rsaKey.getKeyID())
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claimsBuilder.build());
            signedJWT.sign(new RSASSASigner(rsaKey));
            return signedJWT.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize RefreshToken to JWT", e);
        }
    }
}
