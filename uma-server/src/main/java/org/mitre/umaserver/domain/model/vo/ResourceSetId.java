package org.mitre.umaserver.domain.model.vo;

public record ResourceSetId(String value) {
    public ResourceSetId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ResourceSetId must not be null or blank");
        }
    }
}
