package org.mitre.authserver.infrastructure.adapter.out.messaging;

import org.mitre.authserver.domain.event.AccessTokenIssued;
import org.mitre.authserver.domain.event.AccessTokenRevoked;
import org.mitre.authserver.domain.event.RefreshTokenIssued;
import org.mitre.authserver.domain.event.RefreshTokenRevoked;
import org.mitre.authserver.domain.port.out.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaDomainEventPublisher.class);

    private static final String TOPIC_TOKEN_ISSUED = "auth.token.issued";
    private static final String TOPIC_TOKEN_REVOKED = "auth.token.revoked";
    private static final String TOPIC_REFRESH_ISSUED = "auth.refresh.issued";
    private static final String TOPIC_REFRESH_REVOKED = "auth.refresh.revoked";

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
        if (event instanceof AccessTokenIssued) return TOPIC_TOKEN_ISSUED;
        if (event instanceof AccessTokenRevoked) return TOPIC_TOKEN_REVOKED;
        if (event instanceof RefreshTokenIssued) return TOPIC_REFRESH_ISSUED;
        if (event instanceof RefreshTokenRevoked) return TOPIC_REFRESH_REVOKED;
        return null;
    }

    private String resolveKey(Object event) {
        if (event instanceof AccessTokenIssued e) return e.aggregateId();
        if (event instanceof AccessTokenRevoked e) return e.aggregateId();
        if (event instanceof RefreshTokenIssued e) return e.aggregateId();
        if (event instanceof RefreshTokenRevoked e) return e.aggregateId();
        return null;
    }
}
