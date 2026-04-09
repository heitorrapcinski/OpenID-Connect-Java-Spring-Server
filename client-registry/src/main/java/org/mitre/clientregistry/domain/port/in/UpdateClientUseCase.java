package org.mitre.clientregistry.domain.port.in;

import org.mitre.clientregistry.domain.model.Client;

import java.util.Set;

public interface UpdateClientUseCase {

    Client update(String clientId, UpdateClientCommand command);

    record UpdateClientCommand(
            String clientName,
            String clientDescription,
            Set<String> redirectUris,
            Set<String> grantTypes,
            Set<String> responseTypes,
            Set<String> scope,
            String tokenEndpointAuthMethod,
            String applicationType,
            String subjectType,
            String sectorIdentifierUri,
            String jwksUri,
            Integer accessTokenValiditySeconds,
            Integer refreshTokenValiditySeconds,
            Integer idTokenValiditySeconds,
            String traceId
    ) {}
}
