package org.mitre.clientregistry.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SpringDataClientRepository extends MongoRepository<ClientDocument, String> {

    Optional<ClientDocument> findByClientId(String clientId);
}
