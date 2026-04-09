package org.mitre.oidcprovider.application.service;

import com.nimbusds.jose.jwk.JWKSet;
import org.mitre.oidcprovider.domain.port.in.GetJwksUseCase;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class JwksService implements GetJwksUseCase {

    private final JWKSet jwkSet;

    public JwksService(JWKSet jwkSet) {
        this.jwkSet = jwkSet;
    }

    @Override
    public Map<String, Object> getJwks() {
        return jwkSet.toPublicJWKSet().toJSONObject();
    }
}
