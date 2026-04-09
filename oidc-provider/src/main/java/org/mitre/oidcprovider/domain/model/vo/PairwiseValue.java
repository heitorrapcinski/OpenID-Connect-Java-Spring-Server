package org.mitre.oidcprovider.domain.model.vo;

public record PairwiseValue(String value) {
    public PairwiseValue {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("PairwiseValue must not be null or blank");
        }
    }
}
