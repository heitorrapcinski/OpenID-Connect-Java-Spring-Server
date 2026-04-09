package org.mitre.authserver.domain.model.vo;

public record PKCEChallenge(String challenge, String method) {
    public PKCEChallenge {
        if (challenge == null || challenge.isBlank()) {
            throw new IllegalArgumentException("PKCE challenge must not be null or blank");
        }
        if (!"S256".equals(method) && !"plain".equals(method)) {
            throw new IllegalArgumentException("PKCE method must be 'S256' or 'plain', got: " + method);
        }
    }
}
