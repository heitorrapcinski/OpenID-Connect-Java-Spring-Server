package org.mitre.umaserver.domain.event;

import java.time.Instant;

public record ResourceSetRegistered(
        String eventId,
        String eventType,
        String aggregateId,
        Instant occurredAt,
        String serviceOrigin,
        String traceId,
        ResourceSetRegisteredPayload payload
) {
    public record ResourceSetRegisteredPayload(
            String resourceSetId,
            String name,
            String owner,
            String clientId
    ) {}
}
