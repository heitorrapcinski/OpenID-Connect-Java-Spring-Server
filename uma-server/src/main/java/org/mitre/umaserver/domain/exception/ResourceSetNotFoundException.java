package org.mitre.umaserver.domain.exception;

public class ResourceSetNotFoundException extends DomainException {
    public ResourceSetNotFoundException(String id) {
        super("ResourceSet not found: " + id);
    }
}
