package org.mitre.oidcprovider.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Aggregate Root representing a user-approved OAuth2 site.
 * No framework annotations — pure domain object.
 */
public class ApprovedSite {

    private final String id;
    private final String userId;
    private final String clientId;
    private final Instant creationDate;
    private Instant accessDate;
    private Instant timeoutDate;
    private final Set<String> allowedScopes;

    private ApprovedSite(String id, String userId, String clientId, Instant creationDate,
                         Instant accessDate, Instant timeoutDate, Set<String> allowedScopes) {
        this.id = id;
        this.userId = userId;
        this.clientId = clientId;
        this.creationDate = creationDate;
        this.accessDate = accessDate;
        this.timeoutDate = timeoutDate;
        this.allowedScopes = allowedScopes != null ? Collections.unmodifiableSet(allowedScopes) : Collections.emptySet();
    }

    /** Factory method — creates a new ApprovedSite with a generated UUID id. */
    public static ApprovedSite create(String userId, String clientId, Set<String> allowedScopes) {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId must not be null or blank");
        if (clientId == null || clientId.isBlank()) throw new IllegalArgumentException("clientId must not be null or blank");
        Instant now = Instant.now();
        return new ApprovedSite(UUID.randomUUID().toString(), userId, clientId, now, now, null, allowedScopes);
    }

    /** Reconstitute from persistence. */
    public static ApprovedSite reconstitute(String id, String userId, String clientId,
                                            Instant creationDate, Instant accessDate,
                                            Instant timeoutDate, Set<String> allowedScopes) {
        return new ApprovedSite(id, userId, clientId, creationDate, accessDate, timeoutDate, allowedScopes);
    }

    public void recordAccess() {
        this.accessDate = Instant.now();
    }

    public boolean isExpired(Instant now) {
        return timeoutDate != null && timeoutDate.isBefore(now);
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getClientId() { return clientId; }
    public Instant getCreationDate() { return creationDate; }
    public Instant getAccessDate() { return accessDate; }
    public Instant getTimeoutDate() { return timeoutDate; }
    public Set<String> getAllowedScopes() { return allowedScopes; }

    public void setTimeoutDate(Instant timeoutDate) { this.timeoutDate = timeoutDate; }
}
