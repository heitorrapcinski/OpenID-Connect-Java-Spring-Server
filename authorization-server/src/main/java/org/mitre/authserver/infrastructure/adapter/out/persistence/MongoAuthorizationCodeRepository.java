package org.mitre.authserver.infrastructure.adapter.out.persistence;

import org.mitre.authserver.domain.exception.OptimisticLockingException;
import org.mitre.authserver.domain.model.AuthorizationCode;
import org.mitre.authserver.domain.port.out.AuthorizationCodeRepository;
import org.mitre.authserver.infrastructure.adapter.out.persistence.document.AuthorizationCodeDocument;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MongoAuthorizationCodeRepository implements AuthorizationCodeRepository {

    private final SpringDataAuthorizationCodeRepository springDataRepo;

    public MongoAuthorizationCodeRepository(SpringDataAuthorizationCodeRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public Optional<AuthorizationCode> findById(String id) {
        return springDataRepo.findById(id).map(AuthorizationCodeDocument::toDomain);
    }

    @Override
    public Optional<AuthorizationCode> findByCode(String code) {
        return springDataRepo.findByCode(code).map(AuthorizationCodeDocument::toDomain);
    }

    @Override
    public AuthorizationCode save(AuthorizationCode code) {
        try {
            return springDataRepo.save(AuthorizationCodeDocument.fromDomain(code)).toDomain();
        } catch (OptimisticLockingFailureException e) {
            throw new OptimisticLockingException("Concurrent modification detected for authorization code: " + code.getId(), e);
        }
    }

    @Override
    public void deleteById(String id) {
        springDataRepo.deleteById(id);
    }
}
