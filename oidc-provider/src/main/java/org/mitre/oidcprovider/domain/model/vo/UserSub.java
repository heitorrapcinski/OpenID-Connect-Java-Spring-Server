package org.mitre.oidcprovider.domain.model.vo;

public record UserSub(String value) {
    public UserSub {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("UserSub must not be null or blank");
        }
    }
}
