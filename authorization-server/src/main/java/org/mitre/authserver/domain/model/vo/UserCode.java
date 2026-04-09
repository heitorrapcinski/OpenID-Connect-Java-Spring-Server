package org.mitre.authserver.domain.model.vo;

public record UserCode(String value) {
    public UserCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("UserCode must not be null or blank");
        }
    }
}
