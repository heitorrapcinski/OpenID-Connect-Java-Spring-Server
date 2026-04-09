package org.mitre.clientregistry.domain.port.out;

import org.mitre.clientregistry.domain.model.Client;

import java.util.Optional;

public interface ClientRepository {

    Optional<Client> findById(String clientId);

    Client save(Client client);

    void deleteById(String clientId);

    boolean existsById(String clientId);
}
