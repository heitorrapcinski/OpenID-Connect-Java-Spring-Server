package org.mitre.clientregistry.domain.model.vo;

public record ClientId(String value) {

    public ClientId {
        if (value == null) {
            throw new IllegalArgumentException("ClientId must not be null");
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException("ClientId must not be blank");
        }
    }
}
