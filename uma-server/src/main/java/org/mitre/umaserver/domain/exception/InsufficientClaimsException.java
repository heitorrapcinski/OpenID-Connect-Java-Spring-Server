package org.mitre.umaserver.domain.exception;

public class InsufficientClaimsException extends DomainException {
    public InsufficientClaimsException() {
        super("Insufficient claims to satisfy resource policy");
    }
}
