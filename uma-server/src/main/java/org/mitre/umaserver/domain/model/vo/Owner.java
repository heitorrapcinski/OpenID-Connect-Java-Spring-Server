package org.mitre.umaserver.domain.model.vo;

public record Owner(String value) {
    public Owner {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Owner must not be null or blank");
        }
    }
}
