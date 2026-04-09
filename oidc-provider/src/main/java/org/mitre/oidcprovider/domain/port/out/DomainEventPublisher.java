package org.mitre.oidcprovider.domain.port.out;

public interface DomainEventPublisher {
    void publish(Object event);
}
