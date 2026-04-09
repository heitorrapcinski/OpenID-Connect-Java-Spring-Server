package org.mitre.oidcprovider.infrastructure.adapter.out.persistence;

import org.mitre.oidcprovider.infrastructure.adapter.out.persistence.document.BlacklistedSiteDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SpringDataBlacklistedSiteRepository extends MongoRepository<BlacklistedSiteDocument, String> {
    Optional<BlacklistedSiteDocument> findByUri(String uri);
}
