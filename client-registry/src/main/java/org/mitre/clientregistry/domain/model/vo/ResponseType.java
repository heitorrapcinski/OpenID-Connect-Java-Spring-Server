package org.mitre.clientregistry.domain.model.vo;

public record ResponseType(String value) {

    public ResponseType {
        if (value == null) {
            throw new IllegalArgumentException("ResponseType must not be null");
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException("ResponseType must not be blank");
        }
    }
}
