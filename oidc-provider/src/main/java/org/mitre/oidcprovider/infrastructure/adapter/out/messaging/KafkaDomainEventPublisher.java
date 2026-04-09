package org.mitre.oidcprovider.infrastructure.adapter.out.messaging;

import org.mitre.oidcprovider.domain.port.out.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaDomainEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaDomainEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(Object event) {
        log.info("Domain event received (type={}): oidc-provider does not publish domain events in the current design",
                event.getClass().getSimpleName());
    }
}
