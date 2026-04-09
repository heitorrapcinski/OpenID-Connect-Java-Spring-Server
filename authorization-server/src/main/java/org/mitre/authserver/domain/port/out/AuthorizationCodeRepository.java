package org.mitre.authserver.domain.port.out;

import org.mitre.authserver.domain.model.AuthorizationCode;

import java.util.Optional;

public interface AuthorizationCodeRepository {
    Optional<AuthorizationCode> findById(String id);
    Optional<AuthorizationCode> findByCode(String code);
    AuthorizationCode save(AuthorizationCode code);
    void deleteById(String id);
}
