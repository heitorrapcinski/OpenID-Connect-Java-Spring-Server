package org.mitre.authserver.infrastructure.adapter.out.persistence;

import org.mitre.authserver.infrastructure.adapter.out.persistence.document.AccessTokenDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataAccessTokenRepository extends MongoRepository<AccessTokenDocument, String> {
    Optional<AccessTokenDocument> findByTokenValue(String tokenValue);
    List<AccessTokenDocument> findByClientId(String clientId);
    List<AccessTokenDocument> findByAuthenticationHolderUserSub(String userSub);
    List<AccessTokenDocument> findByRefreshTokenId(String refreshTokenId);
}
