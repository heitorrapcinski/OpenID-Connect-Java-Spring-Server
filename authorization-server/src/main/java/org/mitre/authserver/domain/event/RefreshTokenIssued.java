package org.mitre.authserver.domain.event;

import java.time.Instant;

public record RefreshTokenIssued(
        String eventId,
        String eventType,
        String aggregateId,
        Instant occurredAt,
        String serviceOrigin,
        String traceId,
        RefreshTokenIssuedPayload payload
) {
    public record RefreshTokenIssuedPayload(
            String tokenId,
            String clientId,
            String subject
    ) {}
}
