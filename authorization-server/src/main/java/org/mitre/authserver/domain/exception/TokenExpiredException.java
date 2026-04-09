package org.mitre.authserver.domain.exception;

public class TokenExpiredException extends DomainException {
    public TokenExpiredException(String message) {
        super(message);
    }
}
