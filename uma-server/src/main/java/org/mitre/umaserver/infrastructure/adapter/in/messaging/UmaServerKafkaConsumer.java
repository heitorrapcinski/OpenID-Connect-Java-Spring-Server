package org.mitre.umaserver.infrastructure.adapter.in.messaging;

import org.mitre.umaserver.domain.port.out.PermissionTicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UmaServerKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(UmaServerKafkaConsumer.class);

    private final PermissionTicketRepository permissionTicketRepository;

    public UmaServerKafkaConsumer(PermissionTicketRepository permissionTicketRepository) {
        this.permissionTicketRepository = permissionTicketRepository;
    }

    @KafkaListener(topics = "auth.token.revoked", groupId = "uma-server-token-events")
    public void onTokenRevoked(@Payload Map<String, Object> event) {
        // Log the event; in a full implementation, invalidate RPTs associated with the revoked token
        log.info("Token revoked event received: {}", event);
    }
}
