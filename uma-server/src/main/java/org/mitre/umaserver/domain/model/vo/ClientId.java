package org.mitre.umaserver.domain.model.vo;

public record ClientId(String value) {
    public ClientId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ClientId must not be null or blank");
        }
    }
}
