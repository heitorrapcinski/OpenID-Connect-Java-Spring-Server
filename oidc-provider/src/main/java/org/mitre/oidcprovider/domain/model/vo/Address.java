package org.mitre.oidcprovider.domain.model.vo;

public record Address(
        String formatted,
        String streetAddress,
        String locality,
        String region,
        String postalCode,
        String country
) {
}
