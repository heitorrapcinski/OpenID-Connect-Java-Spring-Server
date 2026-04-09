package org.mitre.authserver.domain.port.in;

public interface RevokeTokenUseCase {

    void revoke(String token, String tokenTypeHint, String clientId, String traceId);
}
