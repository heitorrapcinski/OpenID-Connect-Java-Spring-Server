package org.mitre.oidcprovider.infrastructure.adapter.out.rest;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.mitre.oidcprovider.domain.port.out.TokenIntrospectionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuthorizationServerRestAdapter implements TokenIntrospectionPort {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationServerRestAdapter.class);

    private final RestClient restClient;

    public AuthorizationServerRestAdapter(
            @Value("${authorization.server.url:http://localhost:8080}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    @CircuitBreaker(name = "authorization-server", fallbackMethod = "introspectFallback")
    @Retry(name = "authorization-server")
    public IntrospectionResult introspect(String accessToken) {
        try {
            IntrospectionResponse response = restClient.post()
                    .uri("/introspect")
                    .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                    .body("token=" + accessToken)
                    .retrieve()
                    .body(IntrospectionResponse.class);

            if (response == null) {
                return new IntrospectionResult(false, null, null, null, null);
            }

            return new IntrospectionResult(
                    response.active(),
                    response.sub(),
                    response.client_id(),
                    response.scope(),
                    response.exp()
            );
        } catch (Exception e) {
            log.warn("Failed to introspect token: {}", e.getMessage());
            return new IntrospectionResult(false, null, null, null, null);
        }
    }

    @SuppressWarnings("unused")
    public IntrospectionResult introspectFallback(String accessToken, Throwable t) {
        log.warn("Circuit breaker fallback for token introspection: {}", t.getMessage());
        return new IntrospectionResult(false, null, null, null, null);
    }

    private record IntrospectionResponse(
            boolean active,
            String sub,
            String client_id,
            String scope,
            Long exp
    ) {}
}
