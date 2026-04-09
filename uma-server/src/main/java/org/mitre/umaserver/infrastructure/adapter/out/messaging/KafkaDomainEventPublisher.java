package org.mitre.umaserver.infrastructure.adapter.out.messaging;

import org.mitre.umaserver.domain.event.PermissionTicketCreated;
import org.mitre.umaserver.domain.event.ResourceSetDeleted;
import org.mitre.umaserver.domain.event.ResourceSetRegistered;
import org.mitre.umaserver.domain.port.out.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaDomainEventPublisher.class);

    private static final String TOPIC_RESOURCE_REGISTERED = "uma.resource.registered";
    private static final String TOPIC_PERMISSION_CREATED = "uma.permission.created";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaDomainEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(Object event) {
        String topic = resolveTopic(event);
        if (topic == null) {
            log.warn("No topic configured for event type: {}", event.getClass().getSimpleName());
            return;
        }

        String key = resolveKey(event);
        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event {} to topic {}: {}",
                                event.getClass().getSimpleName(), topic, ex.getMessage(), ex);
                    } else {
                        log.debug("Published event {} to topic {} partition {} offset {}",
                                event.getClass().getSimpleName(), topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    private String resolveTopic(Object event) {
        if (event instanceof ResourceSetRegistered) return TOPIC_RESOURCE_REGISTERED;
        if (event instanceof ResourceSetDeleted) return TOPIC_RESOURCE_REGISTERED;
        if (event instanceof PermissionTicketCreated) return TOPIC_PERMISSION_CREATED;
        return null;
    }

    private String resolveKey(Object event) {
        if (event instanceof ResourceSetRegistered e) return e.aggregateId();
        if (event instanceof ResourceSetDeleted e) return e.aggregateId();
        if (event instanceof PermissionTicketCreated e) return e.aggregateId();
        return null;
    }
}
