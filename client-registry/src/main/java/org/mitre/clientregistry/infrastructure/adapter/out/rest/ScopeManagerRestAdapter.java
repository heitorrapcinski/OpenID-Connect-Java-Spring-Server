package org.mitre.clientregistry.infrastructure.adapter.out.rest;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.mitre.clientregistry.domain.port.out.ScopeQueryPort;
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
    private static final long CACHE_TTL_MS = 5 * 60 * 1000L; // 5 minutes

    private final RestClient restClient;

    // Cache: key -> (values, expiresAt)
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public ScopeManagerRestAdapter(
            @Value("${scope.manager.url:http://localhost:8084}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    @CircuitBreaker(name = "scope-manager", fallbackMethod = "getDefaultScopesFallback")
    @Retry(name = "scope-manager")
    public Set<String> getDefaultScopes() {
        Set<String> result = fetchScopes("/scopes/defaults");
        cache.put("defaults", new CacheEntry(result, Instant.now().toEpochMilli() + CACHE_TTL_MS));
        return result;
    }

    @Override
    @CircuitBreaker(name = "scope-manager", fallbackMethod = "getAllScopesFallback")
    @Retry(name = "scope-manager")
    public Set<String> getAllScopes() {
        Set<String> result = fetchScopes("/scopes");
        cache.put("all", new CacheEntry(result, Instant.now().toEpochMilli() + CACHE_TTL_MS));
        return result;
    }

    @SuppressWarnings("unchecked")
    private Set<String> fetchScopes(String path) {
        List<Map<String, Object>> response = restClient.get()
                .uri(path)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

        if (response == null) return Collections.emptySet();

        return response.stream()
                .filter(item -> item.containsKey("value"))
                .map(item -> (String) item.get("value"))
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unused")
    public Set<String> getDefaultScopesFallback(Throwable t) {
        log.warn("Circuit breaker fallback for getDefaultScopes: {}", t.getMessage());
        CacheEntry entry = cache.get("defaults");
        if (entry != null && entry.isValid()) {
            return entry.values();
        }
        return Collections.emptySet();
    }

    @SuppressWarnings("unused")
    public Set<String> getAllScopesFallback(Throwable t) {
        log.warn("Circuit breaker fallback for getAllScopes: {}", t.getMessage());
        CacheEntry entry = cache.get("all");
        if (entry != null && entry.isValid()) {
            return entry.values();
        }
        return Collections.emptySet();
    }

    private record CacheEntry(Set<String> values, long expiresAt) {
        boolean isValid() {
            return Instant.now().toEpochMilli() < expiresAt;
        }
    }
}
