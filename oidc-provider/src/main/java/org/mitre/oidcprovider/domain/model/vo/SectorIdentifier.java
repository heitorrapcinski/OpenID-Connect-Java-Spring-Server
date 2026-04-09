package org.mitre.oidcprovider.domain.model.vo;

public record SectorIdentifier(String value) {
    public SectorIdentifier {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SectorIdentifier must not be null or blank");
        }
    }
}
