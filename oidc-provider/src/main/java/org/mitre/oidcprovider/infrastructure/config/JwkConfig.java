package org.mitre.oidcprovider.infrastructure.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class JwkConfig {

    @Bean
    public JWKSet jwkSet() throws JOSEException {
        RSAKey rsaKey = new RSAKeyGenerator(2048)
                .keyID(UUID.randomUUID().toString())
                .keyUse(KeyUse.SIGNATURE)
                .generate();
        return new JWKSet(rsaKey);
    }
}
