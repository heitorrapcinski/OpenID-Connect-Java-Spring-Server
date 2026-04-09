package org.mitre.clientregistry.domain.port.out;

public interface DomainEventPublisher {

    void publish(Object event);
}
