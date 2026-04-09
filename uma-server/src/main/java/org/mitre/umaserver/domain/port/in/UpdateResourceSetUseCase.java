package org.mitre.umaserver.domain.port.in;

import org.mitre.umaserver.domain.model.ResourceSet;

import java.util.Set;

public interface UpdateResourceSetUseCase {
    ResourceSet update(String id, String name, String uri, String type, Set<String> scopes, String iconUri);
}
