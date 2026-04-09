package org.mitre.authserver.infrastructure.adapter.out.rest;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.mitre.authserver.domain.port.out.ScopeQueryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class ScopeManagerRestAdapter implements ScopeQueryPort {

    private static final Logger log = LoggerFactory.getLogger(ScopeManagerRestAdapter.class);
    private static final long CACHE_TTL_MS = 5 * 60 * 1000L;

    private final RestClient restClient;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public ScopeManagerRestAdapter(
            @Value("${scope.manager.url:http://localhost:8084}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    @CircuitBreaker(name = "scope-manager", fallbackMethod = "getAllScopesFallback")
    @Retry(name = "scope-manager")
    public Set<String> getAllScopes() {
        List<Map<String, Object>> response = restClient.get()
                .uri("/scopes")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        Set<String> scopes = extractScopeValues(response);
        cache.put("all", new CacheEntry(scopes, Instant.now().toEpochMilli() + CACHE_TTL_MS));
        return scopes;
    }

    @Override
    @CircuitBreaker(name = "scope-manager", fallbackMethod = "isRestrictedFallback")
    @Retry(name = "scope-manager")
    public boolean isRestricted(String scope) {
        List<Map<String, Object>> response = restClient.get()
                .uri("/scopes")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (response == null) return false;
        return response.stream()
                .filter(item -> scope.equals(item.get("value")))
                .findFirst()
                .map(item -> Boolean.TRUE.equals(item.get("restricted")))
                .orElse(false);
    }

    @SuppressWarnings("unused")
    public Set<String> getAllScopesFallback(Throwable t) {
        log.warn("Circuit breaker fallback for getAllScopes: {}", t.getMessage());
        CacheEntry entry = cache.get("all");
        if (entry != null && entry.isValid()) return entry.values();
        return Collections.emptySet();
    }

    @SuppressWarnings("unused")
    public boolean isRestrictedFallback(String scope, Throwable t) {
        log.warn("Circuit breaker fallback for isRestricted({}): {}", scope, t.getMessage());
        return false; // fail open — don't block on fallback
    }

    private Set<String> extractScopeValues(List<Map<String, Object>> response) {
        if (response == null) return Collections.emptySet();
        return response.stream()
                .filter(item -> item.containsKey("value"))
                .map(item -> (String) item.get("value"))
                .collect(Collectors.toSet());
    }

    private record CacheEntry(Set<String> values, long expiresAt) {
        boolean isValid() {
            return Instant.now().toEpochMilli() < expiresAt;
        }
    }
}
