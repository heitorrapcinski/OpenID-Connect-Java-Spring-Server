package org.mitre.clientregistry.domain.model.vo;

public record ClientSecret(String value) {

    public ClientSecret {
        if (value == null) {
            throw new IllegalArgumentException("ClientSecret must not be null");
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException("ClientSecret must not be blank");
        }
    }
}
