package org.mitre.authserver.domain.event;

import java.time.Instant;
import java.util.Set;

public record AccessTokenIssued(
        String eventId,
        String eventType,
        String aggregateId,
        Instant occurredAt,
        String serviceOrigin,
        String traceId,
        AccessTokenIssuedPayload payload
) {
    public record AccessTokenIssuedPayload(
            String tokenId,
            String clientId,
            String subject,
            Set<String> scope
    ) {}
}
