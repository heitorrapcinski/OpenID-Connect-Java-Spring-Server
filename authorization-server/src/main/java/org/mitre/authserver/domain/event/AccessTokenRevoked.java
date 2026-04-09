package org.mitre.authserver.domain.event;

import java.time.Instant;

public record AccessTokenRevoked(
        String eventId,
        String eventType,
        String aggregateId,
        Instant occurredAt,
        String serviceOrigin,
        String traceId,
        AccessTokenRevokedPayload payload
) {
    public record AccessTokenRevokedPayload(
            String tokenId,
            String clientId
    ) {}
}
