package org.mitre.clientregistry.domain.model.vo;

public record GrantType(String value) {

    public GrantType {
        if (value == null) {
            throw new IllegalArgumentException("GrantType must not be null");
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException("GrantType must not be blank");
        }
    }
}
