package org.mitre.authserver.domain.port.out;

import org.mitre.authserver.domain.model.AccessToken;

import java.util.List;
import java.util.Optional;

public interface AccessTokenRepository {
    Optional<AccessToken> findById(String id);
    Optional<AccessToken> findByTokenValue(String tokenValue);
    AccessToken save(AccessToken token);
    void deleteById(String id);
    List<AccessToken> findByClientId(String clientId);
    List<AccessToken> findByUserSub(String userSub);
}
