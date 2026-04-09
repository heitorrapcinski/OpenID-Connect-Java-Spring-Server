package org.mitre.authserver.infrastructure.adapter.out.persistence;

import org.mitre.authserver.domain.exception.OptimisticLockingException;
import org.mitre.authserver.domain.model.RefreshToken;
import org.mitre.authserver.domain.port.out.RefreshTokenRepository;
import org.mitre.authserver.infrastructure.adapter.out.persistence.document.RefreshTokenDocument;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MongoRefreshTokenRepository implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository springDataRepo;

    public MongoRefreshTokenRepository(SpringDataRefreshTokenRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public Optional<RefreshToken> findById(String id) {
        return springDataRepo.findById(id).map(RefreshTokenDocument::toDomain);
    }

    @Override
    public Optional<RefreshToken> findByTokenValue(String tokenValue) {
        return springDataRepo.findByTokenValue(tokenValue).map(RefreshTokenDocument::toDomain);
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        try {
            return springDataRepo.save(RefreshTokenDocument.fromDomain(token)).toDomain();
        } catch (OptimisticLockingFailureException e) {
            throw new OptimisticLockingException("Concurrent modification detected for refresh token: " + token.getId(), e);
        }
    }

    @Override
    public void deleteById(String id) {
        springDataRepo.deleteById(id);
    }

    @Override
    public List<RefreshToken> findByClientId(String clientId) {
        return springDataRepo.findByClientId(clientId).stream()
                .map(RefreshTokenDocument::toDomain)
                .collect(Collectors.toList());
    }
}
