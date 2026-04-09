package org.mitre.umaserver.domain.port.in;

import org.mitre.umaserver.domain.model.ResourceSet;

import java.util.Set;

public interface RegisterResourceSetUseCase {
    ResourceSet register(String name, String uri, String type, Set<String> scopes, String iconUri, String owner, String clientId);
}
