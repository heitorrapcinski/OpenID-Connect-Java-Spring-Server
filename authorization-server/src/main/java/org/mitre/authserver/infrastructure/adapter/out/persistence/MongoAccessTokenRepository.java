package org.mitre.authserver.infrastructure.adapter.out.persistence;

import org.mitre.authserver.domain.exception.OptimisticLockingException;
import org.mitre.authserver.domain.model.AccessToken;
import org.mitre.authserver.domain.port.out.AccessTokenRepository;
import org.mitre.authserver.infrastructure.adapter.out.persistence.document.AccessTokenDocument;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MongoAccessTokenRepository implements AccessTokenRepository {

    private final SpringDataAccessTokenRepository springDataRepo;

    public MongoAccessTokenRepository(SpringDataAccessTokenRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public Optional<AccessToken> findById(String id) {
        return springDataRepo.findById(id).map(AccessTokenDocument::toDomain);
    }

    @Override
    public Optional<AccessToken> findByTokenValue(String tokenValue) {
        return springDataRepo.findByTokenValue(tokenValue).map(AccessTokenDocument::toDomain);
    }

    @Override
    public AccessToken save(AccessToken token) {
        try {
            return springDataRepo.save(AccessTokenDocument.fromDomain(token)).toDomain();
        } catch (OptimisticLockingFailureException e) {
            throw new OptimisticLockingException("Concurrent modification detected for access token: " + token.getId(), e);
        }
    }

    @Override
    public void deleteById(String id) {
        springDataRepo.deleteById(id);
    }

    @Override
    public List<AccessToken> findByClientId(String clientId) {
        return springDataRepo.findByClientId(clientId).stream()
                .map(AccessTokenDocument::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccessToken> findByUserSub(String userSub) {
        return springDataRepo.findByAuthenticationHolderUserSub(userSub).stream()
                .map(AccessTokenDocument::toDomain)
                .collect(Collectors.toList());
    }
}
