package org.mitre.umaserver.domain.port.in;

import org.mitre.umaserver.domain.model.PermissionTicket;

import java.util.Set;

public interface CreatePermissionTicketUseCase {
    PermissionTicket create(String resourceSetId, Set<String> scopes);
}
