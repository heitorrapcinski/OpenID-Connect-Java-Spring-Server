package org.mitre.authserver.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(10)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        registry.circuitBreaker("client-registry", config);
        registry.circuitBreaker("scope-manager", config);
        return registry;
    }

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .build();

        RetryRegistry registry = RetryRegistry.of(config);
        registry.retry("client-registry", config);
        registry.retry("scope-manager", config);
        return registry;
    }
}
