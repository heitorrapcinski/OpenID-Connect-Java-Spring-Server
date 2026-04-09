package org.mitre.oidcprovider.infrastructure.adapter.out.persistence;

import org.mitre.oidcprovider.infrastructure.adapter.out.persistence.document.WhitelistedSiteDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SpringDataWhitelistedSiteRepository extends MongoRepository<WhitelistedSiteDocument, String> {
    Optional<WhitelistedSiteDocument> findByClientId(String clientId);
}
