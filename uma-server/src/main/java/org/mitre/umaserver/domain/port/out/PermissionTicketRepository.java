package org.mitre.umaserver.domain.port.out;

import org.mitre.umaserver.domain.model.PermissionTicket;

import java.util.List;
import java.util.Optional;

public interface PermissionTicketRepository {
    PermissionTicket save(PermissionTicket ticket);
    Optional<PermissionTicket> findByTicketValue(String ticketValue);
    List<PermissionTicket> findByResourceSetId(String resourceSetId);
    void deleteByResourceSetId(String resourceSetId);
}
