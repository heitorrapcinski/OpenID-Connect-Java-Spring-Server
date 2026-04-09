package org.mitre.umaserver.infrastructure.adapter.out.persistence;

import org.mitre.umaserver.domain.model.PermissionTicket;
import org.mitre.umaserver.domain.port.out.PermissionTicketRepository;
import org.mitre.umaserver.infrastructure.adapter.out.persistence.document.PermissionTicketDocument;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MongoPermissionTicketRepository implements PermissionTicketRepository {

    private final SpringDataPermissionTicketRepository springDataRepo;

    public MongoPermissionTicketRepository(SpringDataPermissionTicketRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public PermissionTicket save(PermissionTicket ticket) {
        PermissionTicketDocument doc = PermissionTicketDocument.fromDomain(ticket);
        PermissionTicketDocument saved = springDataRepo.save(doc);
        return saved.toDomain();
    }

    @Override
    public Optional<PermissionTicket> findByTicketValue(String ticketValue) {
        return springDataRepo.findByTicket(ticketValue).map(PermissionTicketDocument::toDomain);
    }

    @Override
    public List<PermissionTicket> findByResourceSetId(String resourceSetId) {
        return springDataRepo.findByPermissionResourceSetId(resourceSetId).stream()
                .map(PermissionTicketDocument::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByResourceSetId(String resourceSetId) {
        springDataRepo.deleteByPermissionResourceSetId(resourceSetId);
    }
}
