package org.mitre.authserver.domain.model.vo;

public record TokenValue(String value) {
    public TokenValue {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("TokenValue must not be null or blank");
        }
    }
}
