package org.mitre.clientregistry.domain.exception;

public class ClientAlreadyExistsException extends RuntimeException {

    public ClientAlreadyExistsException(String message) {
        super(message);
    }
}
