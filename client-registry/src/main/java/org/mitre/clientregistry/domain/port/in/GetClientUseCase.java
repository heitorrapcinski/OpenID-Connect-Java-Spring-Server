package org.mitre.clientregistry.domain.port.in;

import org.mitre.clientregistry.domain.model.Client;

public interface GetClientUseCase {

    Client getById(String clientId);
}
