package org.mitre.authserver.application.service;

import org.mitre.authserver.domain.model.AccessToken;
import org.mitre.authserver.domain.port.in.IntrospectTokenUseCase;
import org.mitre.authserver.domain.port.out.AccessTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TokenIntrospectionService implements IntrospectTokenUseCase {

    private final AccessTokenRepository accessTokenRepository;
    private final Clock clock;

    public TokenIntrospectionService(AccessTokenRepository accessTokenRepository, Clock clock) {
        this.accessTokenRepository = accessTokenRepository;
        this.clock = clock;
    }

    @Override
    public IntrospectionResult introspect(String token, String clientId) {
        Optional<AccessToken> found = accessTokenRepository.findByTokenValue(token);
        if (found.isEmpty()) {
            return IntrospectionResult.inactive();
        }

        AccessToken accessToken = found.get();
        if (accessToken.isExpired(clock.instant())) {
            return IntrospectionResult.inactive();
        }

        String sub = accessToken.getSubject() != null ? accessToken.getSubject().value() : null;
        String cid = accessToken.getClientId() != null ? accessToken.getClientId().value() : null;
        String scope = accessToken.getScope().values().stream()
                .sorted().collect(Collectors.joining(" "));
        long exp = accessToken.getExpiration().getEpochSecond();
        long iat = accessToken.getExpiration().minusSeconds(3600).getEpochSecond(); // approximate

        return new IntrospectionResult(true, sub, cid, scope, exp, iat, accessToken.getTokenType());
    }
}
