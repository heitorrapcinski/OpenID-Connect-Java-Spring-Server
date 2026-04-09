package org.mitre.oidcprovider.domain.model;

import java.util.UUID;

/**
 * Aggregate representing a blacklisted (blocked) URI.
 * No framework annotations — pure domain object.
 */
public class BlacklistedSite {

    private final String id;
    private final String uri;

    private BlacklistedSite(String id, String uri) {
        this.id = id;
        this.uri = uri;
    }

    /** Factory method — creates a new BlacklistedSite with a generated UUID id. */
    public static BlacklistedSite create(String uri) {
        if (uri == null || uri.isBlank()) throw new IllegalArgumentException("uri must not be null or blank");
        return new BlacklistedSite(UUID.randomUUID().toString(), uri);
    }

    /** Reconstitute from persistence. */
    public static BlacklistedSite reconstitute(String id, String uri) {
        return new BlacklistedSite(id, uri);
    }

    public String getId() { return id; }
    public String getUri() { return uri; }
}
