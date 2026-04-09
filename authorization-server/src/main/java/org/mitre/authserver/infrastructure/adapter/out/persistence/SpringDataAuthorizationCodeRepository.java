package org.mitre.authserver.infrastructure.adapter.out.persistence;

import org.mitre.authserver.infrastructure.adapter.out.persistence.document.AuthorizationCodeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SpringDataAuthorizationCodeRepository extends MongoRepository<AuthorizationCodeDocument, String> {
    Optional<AuthorizationCodeDocument> findByCode(String code);
}
