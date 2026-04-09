package org.mitre.umaserver.domain.exception;

public class PermissionTicketExpiredException extends DomainException {
    public PermissionTicketExpiredException() {
        super("Permission ticket has expired");
    }
}
