package org.mitre.scopemanager.application.service;

import org.mitre.scopemanager.domain.exception.ScopeAlreadyExistsException;
import org.mitre.scopemanager.domain.exception.ScopeNotFoundException;
import org.mitre.scopemanager.domain.model.SystemScope;
import org.mitre.scopemanager.domain.model.vo.ScopeValue;
import org.mitre.scopemanager.domain.port.in.CreateScopeUseCase;
import org.mitre.scopemanager.domain.port.in.DeleteScopeUseCase;
import org.mitre.scopemanager.domain.port.in.ListScopesUseCase;
import org.mitre.scopemanager.domain.port.in.UpdateScopeUseCase;
import org.mitre.scopemanager.domain.port.out.SystemScopeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScopeApplicationService
        implements CreateScopeUseCase, UpdateScopeUseCase, DeleteScopeUseCase, ListScopesUseCase {

    private final SystemScopeRepository repository;

    public ScopeApplicationService(SystemScopeRepository repository) {
        this.repository = repository;
    }

    @Override
    public SystemScope create(String value, String description, String icon,
                              boolean defaultScope, boolean restricted) {
        repository.findByValue(value).ifPresent(existing -> {
            throw new ScopeAlreadyExistsException("Scope already exists: " + value);
        });
        SystemScope scope = SystemScope.create(new ScopeValue(value), description, icon, defaultScope, restricted);
        return repository.save(scope);
    }

    @Override
    public SystemScope update(String value, String description, String icon,
                              boolean defaultScope, boolean restricted) {
        SystemScope existing = repository.findByValue(value)
                .orElseThrow(() -> new ScopeNotFoundException("Scope not found: " + value));
        SystemScope updated = existing.update(description, icon, defaultScope, restricted);
        return repository.save(updated);
    }

    @Override
    public void delete(String value) {
        repository.findByValue(value)
                .orElseThrow(() -> new ScopeNotFoundException("Scope not found: " + value));
        repository.deleteByValue(value);
    }

    @Override
    public List<SystemScope> listAll() {
        return repository.findAll();
    }

    @Override
    public List<SystemScope> listDefaults() {
        return repository.findAllDefault();
    }
}
