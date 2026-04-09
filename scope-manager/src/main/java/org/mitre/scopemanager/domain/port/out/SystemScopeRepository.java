package org.mitre.scopemanager.domain.port.out;

import org.mitre.scopemanager.domain.model.SystemScope;

import java.util.List;
import java.util.Optional;

public interface SystemScopeRepository {

    List<SystemScope> findAll();

    Optional<SystemScope> findByValue(String value);

    List<SystemScope> findAllDefault();

    SystemScope save(SystemScope scope);

    void deleteByValue(String value);
}
