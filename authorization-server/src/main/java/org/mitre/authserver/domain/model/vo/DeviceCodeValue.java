package org.mitre.authserver.domain.model.vo;

public record DeviceCodeValue(String value) {
    public DeviceCodeValue {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("DeviceCodeValue must not be null or blank");
        }
    }
}
