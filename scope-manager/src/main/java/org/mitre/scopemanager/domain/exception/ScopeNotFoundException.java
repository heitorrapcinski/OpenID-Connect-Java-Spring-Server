package org.mitre.scopemanager.domain.exception;

public class ScopeNotFoundException extends RuntimeException {

    public ScopeNotFoundException(String message) {
        super(message);
    }
}
