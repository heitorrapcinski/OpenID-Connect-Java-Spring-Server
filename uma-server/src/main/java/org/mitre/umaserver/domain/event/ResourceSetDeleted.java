package org.mitre.umaserver.domain.event;

import java.time.Instant;

public record ResourceSetDeleted(
        String eventId,
        String eventType,
        String aggregateId,
        Instant occurredAt,
        String serviceOrigin,
        String traceId,
        ResourceSetDeletedPayload payload
) {
    public record ResourceSetDeletedPayload(
            String resourceSetId,
            String owner
    ) {}
}
