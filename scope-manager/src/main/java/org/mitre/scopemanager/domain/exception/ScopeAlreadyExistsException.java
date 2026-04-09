package org.mitre.scopemanager.domain.exception;

public class ScopeAlreadyExistsException extends RuntimeException {

    public ScopeAlreadyExistsException(String message) {
        super(message);
    }
}
