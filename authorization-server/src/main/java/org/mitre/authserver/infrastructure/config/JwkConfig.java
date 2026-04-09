package org.mitre.authserver.infrastructure.config;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.mitre.authserver.application.service.TokenParser;
import org.mitre.authserver.application.service.TokenSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class JwkConfig {

    @Bean
    public RSAKey rsaKey() throws Exception {
        return new RSAKeyGenerator(2048)
                .keyID("auth-server-key-1")
                .generate();
    }

    @Bean
    public TokenSerializer tokenSerializer(RSAKey rsaKey) {
        return new TokenSerializer(rsaKey);
    }

    @Bean
    public TokenParser tokenParser(RSAKey rsaKey) {
        return new TokenParser(rsaKey);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
