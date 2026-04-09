package org.mitre.scopemanager.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for SystemScopeDocument.
 * Req 5.2: No JPA/SQL dependencies — uses MongoRepository only.
 */
public interface SpringDataSystemScopeRepository extends MongoRepository<SystemScopeDocument, String> {

    List<SystemScopeDocument> findByDefaultScopeTrue();
}
