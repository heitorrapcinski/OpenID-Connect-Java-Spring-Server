package org.mitre.umaserver.domain.port.out;

public interface DomainEventPublisher {
    void publish(Object event);
}
