package org.mitre.umaserver.application.service;

import org.mitre.umaserver.domain.event.PermissionTicketCreated;
import org.mitre.umaserver.domain.exception.ResourceSetNotFoundException;
import org.mitre.umaserver.domain.model.PermissionTicket;
import org.mitre.umaserver.domain.model.vo.Permission;
import org.mitre.umaserver.domain.model.vo.TicketValue;
import org.mitre.umaserver.domain.port.in.CreatePermissionTicketUseCase;
import org.mitre.umaserver.domain.port.out.DomainEventPublisher;
import org.mitre.umaserver.domain.port.out.PermissionTicketRepository;
import org.mitre.umaserver.domain.port.out.ResourceSetRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

@Service
public class PermissionTicketService implements CreatePermissionTicketUseCase {

    private final ResourceSetRepository resourceSetRepository;
    private final PermissionTicketRepository permissionTicketRepository;
    private final DomainEventPublisher eventPublisher;

    public PermissionTicketService(ResourceSetRepository resourceSetRepository,
                                    PermissionTicketRepository permissionTicketRepository,
                                    DomainEventPublisher eventPublisher) {
        this.resourceSetRepository = resourceSetRepository;
        this.permissionTicketRepository = permissionTicketRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PermissionTicket create(String resourceSetId, Set<String> scopes) {
        resourceSetRepository.findById(resourceSetId)
                .orElseThrow(() -> new ResourceSetNotFoundException(resourceSetId));

        TicketValue ticketValue = new TicketValue(UUID.randomUUID().toString());
        Permission permission = new Permission(resourceSetId, scopes);
        Instant expiration = Instant.now().plus(10, ChronoUnit.MINUTES);

        PermissionTicket ticket = PermissionTicket.create(ticketValue, expiration, permission);
        PermissionTicket saved = permissionTicketRepository.save(ticket);

        eventPublisher.publish(new PermissionTicketCreated(
                UUID.randomUUID().toString(),
                "PermissionTicketCreated",
                saved.getId(),
                Instant.now(),
                "uma-server",
                "",
                new PermissionTicketCreated.PermissionTicketCreatedPayload(
                        saved.getTicket().value(),
                        resourceSetId,
                        scopes
                )
        ));

        return saved;
    }
}
