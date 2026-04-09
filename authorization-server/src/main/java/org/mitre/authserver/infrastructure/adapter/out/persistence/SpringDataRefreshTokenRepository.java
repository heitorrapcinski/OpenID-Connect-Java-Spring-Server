package org.mitre.authserver.infrastructure.adapter.out.persistence;

import org.mitre.authserver.infrastructure.adapter.out.persistence.document.RefreshTokenDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataRefreshTokenRepository extends MongoRepository<RefreshTokenDocument, String> {
    Optional<RefreshTokenDocument> findByTokenValue(String tokenValue);
    List<RefreshTokenDocument> findByClientId(String clientId);
}
