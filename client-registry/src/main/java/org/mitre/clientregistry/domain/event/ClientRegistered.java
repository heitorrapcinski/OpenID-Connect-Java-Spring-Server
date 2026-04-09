package org.mitre.clientregistry.domain.event;

import java.time.Instant;
import java.util.Set;

public record ClientRegistered(
        String eventId,
        String eventType,
        String aggregateId,
        Instant occurredAt,
        String serviceOrigin,
        String traceId,
        ClientRegisteredPayload payload
) {
    public ClientRegistered {
        if (eventType == null) eventType = "ClientRegistered";
        if (serviceOrigin == null) serviceOrigin = "client-registry";
    }

    public record ClientRegisteredPayload(
            String clientId,
            String clientName,
            Set<String> grantTypes,
            String subjectType,
            String sectorIdentifierUri
    ) {}
}
