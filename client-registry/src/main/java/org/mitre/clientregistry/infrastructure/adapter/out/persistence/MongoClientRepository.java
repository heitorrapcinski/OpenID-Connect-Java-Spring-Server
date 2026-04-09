package org.mitre.clientregistry.infrastructure.adapter.out.persistence;

import org.mitre.clientregistry.domain.exception.OptimisticLockingException;
import org.mitre.clientregistry.domain.model.Client;
import org.mitre.clientregistry.domain.port.out.ClientRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MongoClientRepository implements ClientRepository {

    private final SpringDataClientRepository springDataRepo;

    public MongoClientRepository(SpringDataClientRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public Optional<Client> findById(String clientId) {
        return springDataRepo.findById(clientId).map(ClientDocument::toDomain);
    }

    @Override
    public Client save(Client client) {
        try {
            ClientDocument doc = ClientDocument.fromDomain(client);
            return springDataRepo.save(doc).toDomain();
        } catch (OptimisticLockingFailureException e) {
            throw new OptimisticLockingException("Concurrent modification detected for client: "
                    + client.getClientId().value(), e);
        }
    }

    @Override
    public void deleteById(String clientId) {
        springDataRepo.deleteById(clientId);
    }

    @Override
    public boolean existsById(String clientId) {
        return springDataRepo.existsById(clientId);
    }
}
