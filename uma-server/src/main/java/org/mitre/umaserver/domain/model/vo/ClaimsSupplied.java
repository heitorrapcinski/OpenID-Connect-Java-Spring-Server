package org.mitre.umaserver.domain.model.vo;

import java.util.List;

public record ClaimsSupplied(
        String name,
        String claimType,
        String value,
        List<String> claimTokenFormat,
        List<String> issuer
) {
    public ClaimsSupplied {
        if (name == null) {
            throw new IllegalArgumentException("ClaimsSupplied name must not be null");
        }
    }
}
