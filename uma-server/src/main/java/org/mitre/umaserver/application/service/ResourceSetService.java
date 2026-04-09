package org.mitre.umaserver.application.service;

import org.mitre.umaserver.domain.event.ResourceSetDeleted;
import org.mitre.umaserver.domain.event.ResourceSetRegistered;
import org.mitre.umaserver.domain.exception.ResourceSetNotFoundException;
import org.mitre.umaserver.domain.model.ResourceSet;
import org.mitre.umaserver.domain.model.vo.ClientId;
import org.mitre.umaserver.domain.model.vo.Owner;
import org.mitre.umaserver.domain.model.vo.ResourceSetId;
import org.mitre.umaserver.domain.port.in.DeleteResourceSetUseCase;
import org.mitre.umaserver.domain.port.in.GetResourceSetUseCase;
import org.mitre.umaserver.domain.port.in.RegisterResourceSetUseCase;
import org.mitre.umaserver.domain.port.in.UpdateResourceSetUseCase;
import org.mitre.umaserver.domain.port.out.DomainEventPublisher;
import org.mitre.umaserver.domain.port.out.PermissionTicketRepository;
import org.mitre.umaserver.domain.port.out.ResourceSetRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ResourceSetService implements RegisterResourceSetUseCase, GetResourceSetUseCase,
        UpdateResourceSetUseCase, DeleteResourceSetUseCase {

    private final ResourceSetRepository resourceSetRepository;
    private final PermissionTicketRepository permissionTicketRepository;
    private final DomainEventPublisher eventPublisher;

    public ResourceSetService(ResourceSetRepository resourceSetRepository,
                               PermissionTicketRepository permissionTicketRepository,
                               DomainEventPublisher eventPublisher) {
        this.resourceSetRepository = resourceSetRepository;
        this.permissionTicketRepository = permissionTicketRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ResourceSet register(String name, String uri, String type, Set<String> scopes,
                                 String iconUri, String owner, String clientId) {
        ResourceSetId id = new ResourceSetId(UUID.randomUUID().toString());
        Owner ownerVo = new Owner(owner);
        ClientId clientIdVo = new ClientId(clientId);

        ResourceSet resourceSet = ResourceSet.create(id, name, ownerVo, clientIdVo);
        resourceSet.update(name, uri, type, scopes, iconUri);

        ResourceSet saved = resourceSetRepository.save(resourceSet);

        eventPublisher.publish(new ResourceSetRegistered(
                UUID.randomUUID().toString(),
                "ResourceSetRegistered",
                saved.getId().value(),
                Instant.now(),
                "uma-server",
                "",
                new ResourceSetRegistered.ResourceSetRegisteredPayload(
                        saved.getId().value(),
                        saved.getName(),
                        saved.getOwner().value(),
                        saved.getClientId().value()
                )
        ));

        return saved;
    }

    @Override
    public ResourceSet getById(String id) {
        return resourceSetRepository.findById(id)
                .orElseThrow(() -> new ResourceSetNotFoundException(id));
    }

    @Override
    public List<ResourceSet> getByOwner(String owner) {
        return resourceSetRepository.findByOwner(owner);
    }

    @Override
    public ResourceSet update(String id, String name, String uri, String type, Set<String> scopes, String iconUri) {
        ResourceSet resourceSet = resourceSetRepository.findById(id)
                .orElseThrow(() -> new ResourceSetNotFoundException(id));

        resourceSet.update(name, uri, type, scopes, iconUri);
        ResourceSet saved = resourceSetRepository.save(resourceSet);

        eventPublisher.publish(new ResourceSetRegistered(
                UUID.randomUUID().toString(),
                "ResourceSetRegistered",
                saved.getId().value(),
                Instant.now(),
                "uma-server",
                "",
                new ResourceSetRegistered.ResourceSetRegisteredPayload(
                        saved.getId().value(),
                        saved.getName(),
                        saved.getOwner().value(),
                        saved.getClientId().value()
                )
        ));

        return saved;
    }

    @Override
    public void delete(String id) {
        ResourceSet resourceSet = resourceSetRepository.findById(id)
                .orElseThrow(() -> new ResourceSetNotFoundException(id));

        permissionTicketRepository.deleteByResourceSetId(id);
        resourceSetRepository.deleteById(id);

        eventPublisher.publish(new ResourceSetDeleted(
                UUID.randomUUID().toString(),
                "ResourceSetDeleted",
                id,
                Instant.now(),
                "uma-server",
                "",
                new ResourceSetDeleted.ResourceSetDeletedPayload(
                        id,
                        resourceSet.getOwner().value()
                )
        ));
    }
}
