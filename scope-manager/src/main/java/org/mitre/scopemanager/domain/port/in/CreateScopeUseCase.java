package org.mitre.scopemanager.domain.port.in;

import org.mitre.scopemanager.domain.model.SystemScope;

public interface CreateScopeUseCase {

    SystemScope create(String value, String description, String icon,
                       boolean defaultScope, boolean restricted);
}
