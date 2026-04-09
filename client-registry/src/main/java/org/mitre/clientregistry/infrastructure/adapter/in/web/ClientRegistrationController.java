package org.mitre.clientregistry.infrastructure.adapter.in.web;

import org.mitre.clientregistry.domain.model.Client;
import org.mitre.clientregistry.domain.port.in.DeleteClientUseCase;
import org.mitre.clientregistry.domain.port.in.GetClientUseCase;
import org.mitre.clientregistry.domain.port.in.RegisterClientUseCase;
import org.mitre.clientregistry.domain.port.in.RegisterClientUseCase.RegisterClientCommand;
import org.mitre.clientregistry.domain.port.in.UpdateClientUseCase;
import org.mitre.clientregistry.domain.port.in.UpdateClientUseCase.UpdateClientCommand;
import org.mitre.clientregistry.infrastructure.adapter.in.web.dto.ClientResponse;
import org.mitre.clientregistry.infrastructure.adapter.in.web.dto.RegisterClientRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/register")
public class ClientRegistrationController {

    private final RegisterClientUseCase registerClientUseCase;
    private final GetClientUseCase getClientUseCase;
    private final UpdateClientUseCase updateClientUseCase;
    private final DeleteClientUseCase deleteClientUseCase;
    private final String serverBaseUrl;

    public ClientRegistrationController(
            RegisterClientUseCase registerClientUseCase,
            GetClientUseCase getClientUseCase,
            UpdateClientUseCase updateClientUseCase,
            DeleteClientUseCase deleteClientUseCase,
            @Value("${server.base-url:http://localhost:8081}") String serverBaseUrl) {
        this.registerClientUseCase = registerClientUseCase;
        this.getClientUseCase = getClientUseCase;
        this.updateClientUseCase = updateClientUseCase;
        this.deleteClientUseCase = deleteClientUseCase;
        this.serverBaseUrl = serverBaseUrl;
    }

    @PostMapping
    public ResponseEntity<ClientResponse> registerClient(
            @RequestBody RegisterClientRequest request,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId) {

        String resolvedTraceId = traceId != null ? traceId : UUID.randomUUID().toString();

        RegisterClientCommand command = new RegisterClientCommand(
                request.clientName(),
                request.clientDescription(),
                request.redirectUris(),
                request.grantTypes(),
                request.responseTypes(),
                parseScope(request.scope()),
                request.tokenEndpointAuthMethod(),
                request.applicationType(),
                request.subjectType(),
                request.sectorIdentifierUri(),
                request.jwksUri(),
                request.accessTokenValiditySeconds(),
                request.refreshTokenValiditySeconds(),
                request.idTokenValiditySeconds(),
                resolvedTraceId
        );

        Client client = registerClientUseCase.register(command);
        return ResponseEntity.status(201).body(ClientResponse.fromDomain(client, serverBaseUrl));
    }

    @GetMapping("/{client_id}")
    public ResponseEntity<ClientResponse> getClient(@PathVariable("client_id") String clientId) {
        Client client = getClientUseCase.getById(clientId);
        return ResponseEntity.ok(ClientResponse.fromDomain(client, serverBaseUrl));
    }

    @PutMapping("/{client_id}")
    public ResponseEntity<ClientResponse> updateClient(
            @PathVariable("client_id") String clientId,
            @RequestBody RegisterClientRequest request,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId) {

        String resolvedTraceId = traceId != null ? traceId : UUID.randomUUID().toString();

        UpdateClientCommand command = new UpdateClientCommand(
                request.clientName(),
                request.clientDescription(),
                request.redirectUris(),
                request.grantTypes(),
                request.responseTypes(),
                parseScope(request.scope()),
                request.tokenEndpointAuthMethod(),
                request.applicationType(),
                request.subjectType(),
                request.sectorIdentifierUri(),
                request.jwksUri(),
                request.accessTokenValiditySeconds(),
                request.refreshTokenValiditySeconds(),
                request.idTokenValiditySeconds(),
                resolvedTraceId
        );

        Client client = updateClientUseCase.update(clientId, command);
        return ResponseEntity.ok(ClientResponse.fromDomain(client, serverBaseUrl));
    }

    @DeleteMapping("/{client_id}")
    public ResponseEntity<Void> deleteClient(
            @PathVariable("client_id") String clientId,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId) {

        String resolvedTraceId = traceId != null ? traceId : UUID.randomUUID().toString();
        deleteClientUseCase.delete(clientId, resolvedTraceId);
        return ResponseEntity.noContent().build();
    }

    private Set<String> parseScope(String scope) {
        if (scope == null || scope.isBlank()) return null;
        return new HashSet<>(Arrays.asList(scope.trim().split("\\s+")));
    }
}
