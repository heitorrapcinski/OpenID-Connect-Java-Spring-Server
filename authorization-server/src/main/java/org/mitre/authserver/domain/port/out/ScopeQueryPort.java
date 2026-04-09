package org.mitre.authserver.domain.port.out;

import java.util.Set;

public interface ScopeQueryPort {
    Set<String> getAllScopes();
    boolean isRestricted(String scope);
}
