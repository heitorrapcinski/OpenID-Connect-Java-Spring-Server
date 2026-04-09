package org.mitre.clientregistry.domain.model.vo;

public record RedirectUri(String value) {

    public RedirectUri {
        if (value == null) {
            throw new IllegalArgumentException("RedirectUri must not be null");
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException("RedirectUri must not be blank");
        }
    }
}
