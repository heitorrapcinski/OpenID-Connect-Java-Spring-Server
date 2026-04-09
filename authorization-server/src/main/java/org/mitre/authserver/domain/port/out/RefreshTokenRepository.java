package org.mitre.authserver.domain.port.out;

import org.mitre.authserver.domain.model.RefreshToken;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository {
    Optional<RefreshToken> findById(String id);
    Optional<RefreshToken> findByTokenValue(String tokenValue);
    RefreshToken save(RefreshToken token);
    void deleteById(String id);
    List<RefreshToken> findByClientId(String clientId);
}
