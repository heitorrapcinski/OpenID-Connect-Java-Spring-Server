package org.mitre.oidcprovider.infrastructure.adapter.out.persistence;

import org.mitre.oidcprovider.infrastructure.adapter.out.persistence.document.PairwiseIdentifierDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SpringDataPairwiseIdentifierRepository extends MongoRepository<PairwiseIdentifierDocument, String> {
    Optional<PairwiseIdentifierDocument> findByUserSubAndSectorIdentifier(String userSub, String sectorIdentifier);
}
