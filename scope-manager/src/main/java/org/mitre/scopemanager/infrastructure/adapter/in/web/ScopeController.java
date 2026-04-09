package org.mitre.scopemanager.infrastructure.adapter.in.web;

import org.mitre.scopemanager.domain.port.in.CreateScopeUseCase;
import org.mitre.scopemanager.domain.port.in.DeleteScopeUseCase;
import org.mitre.scopemanager.domain.port.in.ListScopesUseCase;
import org.mitre.scopemanager.domain.port.in.UpdateScopeUseCase;
import org.mitre.scopemanager.infrastructure.adapter.in.web.dto.ScopeRequest;
import org.mitre.scopemanager.infrastructure.adapter.in.web.dto.ScopeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/scopes")
public class ScopeController {

    private final ListScopesUseCase listScopesUseCase;
    private final CreateScopeUseCase createScopeUseCase;
    private final UpdateScopeUseCase updateScopeUseCase;
    private final DeleteScopeUseCase deleteScopeUseCase;

    public ScopeController(ListScopesUseCase listScopesUseCase,
                           CreateScopeUseCase createScopeUseCase,
                           UpdateScopeUseCase updateScopeUseCase,
                           DeleteScopeUseCase deleteScopeUseCase) {
        this.listScopesUseCase = listScopesUseCase;
        this.createScopeUseCase = createScopeUseCase;
        this.updateScopeUseCase = updateScopeUseCase;
        this.deleteScopeUseCase = deleteScopeUseCase;
    }

    @GetMapping
    public List<ScopeResponse> listAll() {
        return listScopesUseCase.listAll().stream()
                .map(ScopeResponse::fromDomain)
                .toList();
    }

    @GetMapping("/defaults")
    public List<ScopeResponse> listDefaults() {
        return listScopesUseCase.listDefaults().stream()
                .map(ScopeResponse::fromDomain)
                .toList();
    }

    @PostMapping
    public ResponseEntity<ScopeResponse> create(@RequestBody ScopeRequest request) {
        var scope = createScopeUseCase.create(
                request.value(),
                request.description(),
                request.icon(),
                request.defaultScope(),
                request.restricted()
        );
        return ResponseEntity.status(201).body(ScopeResponse.fromDomain(scope));
    }

    @PutMapping("/{value}")
    public ScopeResponse update(@PathVariable String value, @RequestBody ScopeRequest request) {
        var scope = updateScopeUseCase.update(
                value,
                request.description(),
                request.icon(),
                request.defaultScope(),
                request.restricted()
        );
        return ScopeResponse.fromDomain(scope);
    }

    @DeleteMapping("/{value}")
    public ResponseEntity<Void> delete(@PathVariable String value) {
        deleteScopeUseCase.delete(value);
        return ResponseEntity.noContent().build();
    }
}
