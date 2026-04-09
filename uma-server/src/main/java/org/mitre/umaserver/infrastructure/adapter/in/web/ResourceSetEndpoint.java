package org.mitre.umaserver.infrastructure.adapter.in.web;

import org.mitre.umaserver.domain.model.ResourceSet;
import org.mitre.umaserver.domain.port.in.DeleteResourceSetUseCase;
import org.mitre.umaserver.domain.port.in.GetResourceSetUseCase;
import org.mitre.umaserver.domain.port.in.RegisterResourceSetUseCase;
import org.mitre.umaserver.domain.port.in.UpdateResourceSetUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/uma/resource_set")
public class ResourceSetEndpoint {

    private final RegisterResourceSetUseCase registerUseCase;
    private final GetResourceSetUseCase getUseCase;
    private final UpdateResourceSetUseCase updateUseCase;
    private final DeleteResourceSetUseCase deleteUseCase;

    public ResourceSetEndpoint(RegisterResourceSetUseCase registerUseCase,
                                GetResourceSetUseCase getUseCase,
                                UpdateResourceSetUseCase updateUseCase,
                                DeleteResourceSetUseCase deleteUseCase) {
        this.registerUseCase = registerUseCase;
        this.getUseCase = getUseCase;
        this.updateUseCase = updateUseCase;
        this.deleteUseCase = deleteUseCase;
    }

    public record ResourceSetRequest(
            String name,
            String uri,
            String type,
            Set<String> scopes,
            String iconUri,
            String owner,
            String clientId
    ) {}

    public record ResourceSetResponse(
            String id,
            String name,
            String uri,
            String type,
            Set<String> scopes,
            String iconUri,
            String owner,
            String clientId,
            List<ResourceSet.Policy> policies
    ) {}

    @PostMapping
    public ResponseEntity<Map<String, String>> register(@RequestBody ResourceSetRequest request) {
        ResourceSet resourceSet = registerUseCase.register(
                request.name(), request.uri(), request.type(), request.scopes(),
                request.iconUri(), request.owner(), request.clientId()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("_id", resourceSet.getId().value()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceSetResponse> getById(@PathVariable String id) {
        ResourceSet resourceSet = getUseCase.getById(id);
        return ResponseEntity.ok(toResponse(resourceSet));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResourceSetResponse> update(@PathVariable String id,
                                                       @RequestBody ResourceSetRequest request) {
        ResourceSet resourceSet = updateUseCase.update(
                id, request.name(), request.uri(), request.type(), request.scopes(), request.iconUri()
        );
        return ResponseEntity.ok(toResponse(resourceSet));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ResourceSetResponse>> getByOwner(@RequestParam String owner) {
        List<ResourceSetResponse> responses = getUseCase.getByOwner(owner).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    private ResourceSetResponse toResponse(ResourceSet rs) {
        return new ResourceSetResponse(
                rs.getId().value(),
                rs.getName(),
                rs.getUri(),
                rs.getType(),
                rs.getScopes(),
                rs.getIconUri(),
                rs.getOwner().value(),
                rs.getClientId() != null ? rs.getClientId().value() : null,
                rs.getPolicies()
        );
    }
}
