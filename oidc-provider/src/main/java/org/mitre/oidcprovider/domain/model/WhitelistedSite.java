package org.mitre.oidcprovider.domain.model;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Aggregate representing a whitelisted (pre-approved) OAuth2 site.
 * No framework annotations — pure domain object.
 */
public class WhitelistedSite {

    private final String id;
    private final String creatorUserId;
    private final String clientId;
    private final Set<String> allowedScopes;

    private WhitelistedSite(String id, String creatorUserId, String clientId, Set<String> allowedScopes) {
        this.id = id;
        this.creatorUserId = creatorUserId;
        this.clientId = clientId;
        this.allowedScopes = allowedScopes != null ? Collections.unmodifiableSet(allowedScopes) : Collections.emptySet();
    }

    /** Factory method — creates a new WhitelistedSite with a generated UUID id. */
    public static WhitelistedSite create(String creatorUserId, String clientId, Set<String> allowedScopes) {
        if (creatorUserId == null || creatorUserId.isBlank()) throw new IllegalArgumentException("creatorUserId must not be null or blank");
        if (clientId == null || clientId.isBlank()) throw new IllegalArgumentException("clientId must not be null or blank");
        return new WhitelistedSite(UUID.randomUUID().toString(), creatorUserId, clientId, allowedScopes);
    }

    /** Reconstitute from persistence. */
    public static WhitelistedSite reconstitute(String id, String creatorUserId, String clientId, Set<String> allowedScopes) {
        return new WhitelistedSite(id, creatorUserId, clientId, allowedScopes);
    }

    public String getId() { return id; }
    public String getCreatorUserId() { return creatorUserId; }
    public String getClientId() { return clientId; }
    public Set<String> getAllowedScopes() { return allowedScopes; }
}
