package org.mitre.clientregistry.infrastructure.adapter.out.messaging;

import org.mitre.clientregistry.domain.event.ClientDeleted;
import org.mitre.clientregistry.domain.event.ClientRegistered;
import org.mitre.clientregistry.domain.event.ClientUpdated;
import org.mitre.clientregistry.domain.port.out.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaDomainEventPublisher.class);

    private static final String TOPIC_CLIENT_REGISTERED = "client.registered";
    private static final String TOPIC_CLIENT_UPDATED = "client.updated";
    private static final String TOPIC_CLIENT_DELETED = "client.deleted";

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
        if (event instanceof ClientRegistered) return TOPIC_CLIENT_REGISTERED;
        if (event instanceof ClientUpdated) return TOPIC_CLIENT_UPDATED;
        if (event instanceof ClientDeleted) return TOPIC_CLIENT_DELETED;
        return null;
    }

    private String resolveKey(Object event) {
        if (event instanceof ClientRegistered e) return e.aggregateId();
        if (event instanceof ClientUpdated e) return e.aggregateId();
        if (event instanceof ClientDeleted e) return e.aggregateId();
        return null;
    }
}
