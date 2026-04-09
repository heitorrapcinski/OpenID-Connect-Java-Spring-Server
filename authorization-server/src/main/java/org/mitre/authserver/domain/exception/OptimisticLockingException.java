package org.mitre.authserver.domain.exception;

public class OptimisticLockingException extends DomainException {
    public OptimisticLockingException(String message) {
        super(message);
    }

    public OptimisticLockingException(String message, Throwable cause) {
        super(message, cause);
    }
}
