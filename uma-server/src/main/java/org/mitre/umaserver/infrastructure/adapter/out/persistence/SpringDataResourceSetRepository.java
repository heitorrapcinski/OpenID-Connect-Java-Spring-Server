package org.mitre.umaserver.infrastructure.adapter.out.persistence;

import org.mitre.umaserver.infrastructure.adapter.out.persistence.document.ResourceSetDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SpringDataResourceSetRepository extends MongoRepository<ResourceSetDocument, String> {
    List<ResourceSetDocument> findByOwner(String owner);
}
