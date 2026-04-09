package org.mitre.scopemanager.domain.port.in;

import org.mitre.scopemanager.domain.model.SystemScope;

import java.util.List;

public interface ListScopesUseCase {

    List<SystemScope> listAll();

    List<SystemScope> listDefaults();
}
