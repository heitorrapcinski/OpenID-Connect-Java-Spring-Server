package org.mitre.oidcprovider.infrastructure.adapter.out.persistence;

import org.mitre.oidcprovider.infrastructure.adapter.out.persistence.document.ApprovedSiteDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataApprovedSiteRepository extends MongoRepository<ApprovedSiteDocument, String> {
    Optional<ApprovedSiteDocument> findByUserIdAndClientId(String userId, String clientId);
    List<ApprovedSiteDocument> findByUserId(String userId);
}
