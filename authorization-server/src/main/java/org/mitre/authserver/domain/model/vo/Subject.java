package org.mitre.authserver.domain.model.vo;

public record Subject(String value) {
    public Subject {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Subject must not be null or blank");
        }
    }
}
