package org.mitre.authserver.domain.exception;

public class InvalidGrantException extends DomainException {
    public InvalidGrantException(String message) {
        super(message);
    }
}
