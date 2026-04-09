package org.mitre.umaserver.domain.event;

import java.time.Instant;
import java.util.Set;

public record PermissionTicketCreated(
        String eventId,
        String eventType,
        String aggregateId,
        Instant occurredAt,
        String serviceOrigin,
        String traceId,
        PermissionTicketCreatedPayload payload
) {
    public record PermissionTicketCreatedPayload(
            String ticketValue,
            String resourceSetId,
            Set<String> scopes
    ) {}
}
