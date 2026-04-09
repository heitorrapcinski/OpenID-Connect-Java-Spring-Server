package org.mitre.clientregistry.domain.event;

import java.time.Instant;

public record ClientDeleted(
        String eventId,
        String eventType,
        String aggregateId,
        Instant occurredAt,
        String serviceOrigin,
        String traceId,
        ClientDeletedPayload payload
) {
    public ClientDeleted {
        if (eventType == null) eventType = "ClientDeleted";
        if (serviceOrigin == null) serviceOrigin = "client-registry";
    }

    public record ClientDeletedPayload(
            String clientId
    ) {}
}
