package org.mitre.authserver.domain.port.out;

public interface DomainEventPublisher {
    void publish(Object event);
}
