package org.mitre.authserver.domain.exception;

public class AuthorizationCodeReusedException extends DomainException {
    public AuthorizationCodeReusedException(String message) {
        super(message);
    }
}
