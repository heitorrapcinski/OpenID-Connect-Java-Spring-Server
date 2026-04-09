package org.mitre.oidcprovider.infrastructure.adapter.out.persistence;

import org.mitre.oidcprovider.infrastructure.adapter.out.persistence.document.UserInfoDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SpringDataUserInfoRepository extends MongoRepository<UserInfoDocument, String> {
    Optional<UserInfoDocument> findBySub(String sub);
    Optional<UserInfoDocument> findByPreferredUsername(String preferredUsername);
}
