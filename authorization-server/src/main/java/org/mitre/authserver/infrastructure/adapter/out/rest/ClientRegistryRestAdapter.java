package org.mitre.authserver.infrastructure.adapter.out.rest;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.mitre.authserver.domain.port.out.ClientQueryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ClientRegistryRestAdapter implements ClientQueryPort {

    private static final Logger log = LoggerFactory.getLogger(ClientRegistryRestAdapter.class);
    private static final long CACHE_TTL_MS = 5 * 60 * 1000L;

    private final RestClient restClient;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public ClientRegistryRestAdapter(
            @Value("${client.registry.url:http://localhost:8081}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    @CircuitBreaker(name = "client-registry", fallbackMethod = "findByIdFallback")
    @Retry(name = "client-registry")
    public Optional<ClientInfo> findById(String clientId) {
        try {
            ClientInfoResponse response = restClient.get()
                    .uri("/register/{clientId}", clientId)
                    .retrieve()
                    .body(ClientInfoResponse.class);

            if (response == null) return Optional.empty();

            ClientInfo info = mapToClientInfo(response);
            cache.put(clientId, new CacheEntry(info, Instant.now().toEpochMilli() + CACHE_TTL_MS));
            return Optional.of(info);
        } catch (Exception e) {
            log.warn("Failed to fetch client {} from registry: {}", clientId, e.getMessage());
            throw e;
        }
    }

    @SuppressWarnings("unused")
    public Optional<ClientInfo> findByIdFallback(String clientId, Throwable t) {
        log.warn("Circuit breaker fallback for client {}: {}", clientId, t.getMessage());
        CacheEntry entry = cache.get(clientId);
        if (entry != null && entry.isValid()) {
            return Optional.of(entry.clientInfo());
        }
        return Optional.empty();
    }

    @KafkaListener(topics = "client.updated", groupId = "auth-server-client-cache")
    public void onClientUpdated(Map<String, Object> event) {
        try {
            Object aggregateId = event.get("aggregateId");
            if (aggregateId instanceof String clientId) {
                cache.remove(clientId);
                log.debug("Invalidated cache for client: {}", clientId);
            }
        } catch (Exception e) {
            log.warn("Failed to process client.updated event: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private ClientInfo mapToClientInfo(ClientInfoResponse response) {
        return new ClientInfo(
                response.clientId(),
                response.grantTypes(),
                response.scope(),
                response.redirectUris(),
                response.accessTokenValiditySeconds(),
                response.refreshTokenValiditySeconds(),
                response.reuseRefreshToken(),
                response.clearAccessTokensOnRefresh(),
                response.allowIntrospection(),
                response.tokenEndpointAuthMethod()
        );
    }

    private record CacheEntry(ClientInfo clientInfo, long expiresAt) {
        boolean isValid() {
            return Instant.now().toEpochMilli() < expiresAt;
        }
    }

    // DTO for deserializing client-registry response
    private record ClientInfoResponse(
            String clientId,
            Set<String> grantTypes,
            Set<String> scope,
            Set<String> redirectUris,
            Integer accessTokenValiditySeconds,
            Integer refreshTokenValiditySeconds,
            boolean reuseRefreshToken,
            boolean clearAccessTokensOnRefresh,
            boolean allowIntrospection,
            String tokenEndpointAuthMethod
    ) {}
}
