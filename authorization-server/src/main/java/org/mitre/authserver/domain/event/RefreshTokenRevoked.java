package org.mitre.authserver.domain.event;

import java.time.Instant;

public record RefreshTokenRevoked(
        String eventId,
        String eventType,
        String aggregateId,
        Instant occurredAt,
        String serviceOrigin,
        String traceId,
        RefreshTokenRevokedPayload payload
) {
    public record RefreshTokenRevokedPayload(
            String tokenId,
            String clientId
    ) {}
}
