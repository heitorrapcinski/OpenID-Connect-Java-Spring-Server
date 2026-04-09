package org.mitre.clientregistry.domain.port.out;

import java.util.Set;

public interface ScopeQueryPort {

    Set<String> getDefaultScopes();

    Set<String> getAllScopes();
}
