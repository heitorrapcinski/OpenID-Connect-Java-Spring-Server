package org.mitre.clientregistry.application.service;

import org.mitre.clientregistry.domain.event.ClientDeleted;
import org.mitre.clientregistry.domain.event.ClientRegistered;
import org.mitre.clientregistry.domain.event.ClientUpdated;
import org.mitre.clientregistry.domain.exception.ClientAlreadyExistsException;
import org.mitre.clientregistry.domain.exception.ClientNotFoundException;
import org.mitre.clientregistry.domain.model.Client;
import org.mitre.clientregistry.domain.model.vo.ClientId;
import org.mitre.clientregistry.domain.model.vo.ClientSecret;
import org.mitre.clientregistry.domain.model.vo.GrantType;
import org.mitre.clientregistry.domain.model.vo.RedirectUri;
import org.mitre.clientregistry.domain.model.vo.ResponseType;
import org.mitre.clientregistry.domain.model.vo.SectorIdentifierUri;
import org.mitre.clientregistry.domain.port.in.DeleteClientUseCase;
import org.mitre.clientregistry.domain.port.in.GetClientUseCase;
import org.mitre.clientregistry.domain.port.in.RegisterClientUseCase;
import org.mitre.clientregistry.domain.port.in.UpdateClientUseCase;
import org.mitre.clientregistry.domain.port.out.ClientRepository;
import org.mitre.clientregistry.domain.port.out.DomainEventPublisher;
import org.mitre.clientregistry.domain.port.out.ScopeQueryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClientRegistrationService
        implements RegisterClientUseCase, GetClientUseCase, UpdateClientUseCase, DeleteClientUseCase {

    private final ClientRepository clientRepository;
    private final ScopeQueryPort scopeQueryPort;
    private final DomainEventPublisher domainEventPublisher;

    public ClientRegistrationService(ClientRepository clientRepository,
                                     ScopeQueryPort scopeQueryPort,
                                     DomainEventPublisher domainEventPublisher) {
        this.clientRepository = clientRepository;
        this.scopeQueryPort = scopeQueryPort;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public Client register(RegisterClientCommand command) {
        Set<String> scope = (command.scope() == null || command.scope().isEmpty())
                ? scopeQueryPort.getDefaultScopes()
                : command.scope();

        String clientId = generateUniqueClientId();
        String clientSecret = UUID.randomUUID().toString();
        String registrationAccessToken = UUID.randomUUID().toString();

        Client client = Client.register(Client.builder()
                .clientId(new ClientId(clientId))
                .clientSecret(new ClientSecret(clientSecret))
                .clientName(command.clientName())
                .clientDescription(command.clientDescription())
                .redirectUris(toRedirectUris(command.redirectUris()))
                .grantTypes(toGrantTypes(command.grantTypes()))
                .responseTypes(toResponseTypes(command.responseTypes()))
                .scope(scope)
                .tokenEndpointAuthMethod(command.tokenEndpointAuthMethod())
                .applicationType(command.applicationType())
                .subjectType(command.subjectType())
                .sectorIdentifierUri(command.sectorIdentifierUri() != null
                        ? new SectorIdentifierUri(command.sectorIdentifierUri()) : null)
                .jwksUri(command.jwksUri())
                .accessTokenValiditySeconds(command.accessTokenValiditySeconds())
                .refreshTokenValiditySeconds(command.refreshTokenValiditySeconds())
                .idTokenValiditySeconds(command.idTokenValiditySeconds())
                .dynamicallyRegistered(true)
                .registrationAccessToken(registrationAccessToken)
                .createdAt(Instant.now()));

        Client saved = clientRepository.save(client);

        domainEventPublisher.publish(new ClientRegistered(
                UUID.randomUUID().toString(),
                "ClientRegistered",
                clientId,
                Instant.now(),
                "client-registry",
                command.traceId(),
                new ClientRegistered.ClientRegisteredPayload(
                        clientId,
                        command.clientName(),
                        command.grantTypes(),
                        command.subjectType(),
                        command.sectorIdentifierUri()
                )
        ));

        return saved;
    }

    @Override
    public Client getById(String clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException("Client not found: " + clientId));
    }

    @Override
    public Client update(String clientId, UpdateClientCommand command) {
        Client existing = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException("Client not found: " + clientId));

        Client updated = existing.update(Client.builder()
                .clientId(existing.getClientId())
                .clientSecret(existing.getClientSecret())
                .clientName(command.clientName())
                .clientDescription(command.clientDescription())
                .redirectUris(toRedirectUris(command.redirectUris()))
                .grantTypes(toGrantTypes(command.grantTypes()))
                .responseTypes(toResponseTypes(command.responseTypes()))
                .scope(command.scope())
                .tokenEndpointAuthMethod(command.tokenEndpointAuthMethod())
                .applicationType(command.applicationType())
                .subjectType(command.subjectType())
                .sectorIdentifierUri(command.sectorIdentifierUri() != null
                        ? new SectorIdentifierUri(command.sectorIdentifierUri()) : null)
                .jwksUri(command.jwksUri())
                .accessTokenValiditySeconds(command.accessTokenValiditySeconds())
                .refreshTokenValiditySeconds(command.refreshTokenValiditySeconds())
                .idTokenValiditySeconds(command.idTokenValiditySeconds())
                .dynamicallyRegistered(existing.isDynamicallyRegistered())
                .registrationAccessToken(existing.getRegistrationAccessToken())
                .createdAt(existing.getCreatedAt()));

        Client saved = clientRepository.save(updated);

        domainEventPublisher.publish(new ClientUpdated(
                UUID.randomUUID().toString(),
                "ClientUpdated",
                clientId,
                Instant.now(),
                "client-registry",
                command.traceId(),
                new ClientUpdated.ClientUpdatedPayload(clientId, command.clientName())
        ));

        return saved;
    }

    @Override
    public void delete(String clientId, String traceId) {
        clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException("Client not found: " + clientId));

        clientRepository.deleteById(clientId);

        domainEventPublisher.publish(new ClientDeleted(
                UUID.randomUUID().toString(),
                "ClientDeleted",
                clientId,
                Instant.now(),
                "client-registry",
                traceId,
                new ClientDeleted.ClientDeletedPayload(clientId)
        ));
    }

    private String generateUniqueClientId() {
        String id;
        do {
            id = UUID.randomUUID().toString();
        } while (clientRepository.existsById(id));
        return id;
    }

    private Set<RedirectUri> toRedirectUris(Set<String> uris) {
        if (uris == null) return null;
        return uris.stream().map(RedirectUri::new).collect(Collectors.toSet());
    }

    private Set<GrantType> toGrantTypes(Set<String> types) {
        if (types == null) return null;
        return types.stream().map(GrantType::new).collect(Collectors.toSet());
    }

    private Set<ResponseType> toResponseTypes(Set<String> types) {
        if (types == null) return null;
        return types.stream().map(ResponseType::new).collect(Collectors.toSet());
    }
}
