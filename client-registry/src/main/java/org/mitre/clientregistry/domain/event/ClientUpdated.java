package org.mitre.clientregistry.domain.event;

import java.time.Instant;

public record ClientUpdated(
        String eventId,
        String eventType,
        String aggregateId,
        Instant occurredAt,
        String serviceOrigin,
        String traceId,
        ClientUpdatedPayload payload
) {
    public ClientUpdated {
        if (eventType == null) eventType = "ClientUpdated";
        if (serviceOrigin == null) serviceOrigin = "client-registry";
    }

    public record ClientUpdatedPayload(
            String clientId,
            String clientName
    ) {}
}
