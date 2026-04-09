package org.mitre.umaserver.infrastructure.adapter.out.persistence;

import org.mitre.umaserver.infrastructure.adapter.out.persistence.document.PermissionTicketDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataPermissionTicketRepository extends MongoRepository<PermissionTicketDocument, String> {
    Optional<PermissionTicketDocument> findByTicket(String ticket);
    List<PermissionTicketDocument> findByPermissionResourceSetId(String resourceSetId);
    void deleteByPermissionResourceSetId(String resourceSetId);
}
