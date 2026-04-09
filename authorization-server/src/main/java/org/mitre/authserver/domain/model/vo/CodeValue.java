package org.mitre.authserver.domain.model.vo;

public record CodeValue(String value) {
    public CodeValue {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CodeValue must not be null or blank");
        }
    }
}
