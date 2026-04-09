package org.mitre.authserver.domain.port.out;

import java.util.Optional;
import java.util.Set;

public interface ClientQueryPort {

    Optional<ClientInfo> findById(String clientId);

    record ClientInfo(
            String clientId,
            Set<String> grantTypes,
            Set<String> scope,
            Set<String> redirectUris,
            Integer accessTokenValiditySeconds,
            Integer refreshTokenValiditySeconds,
            boolean reuseRefreshToken,
            boolean clearAccessTokensOnRefresh,
            boolean allowIntrospection,
            String tokenEndpointAuthMethod
    ) {}
}
