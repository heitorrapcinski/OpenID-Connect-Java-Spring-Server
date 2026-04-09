package org.mitre.authserver.application.service;

import org.mitre.authserver.domain.model.AccessToken;
import org.mitre.authserver.domain.model.RefreshToken;
import org.mitre.authserver.domain.port.in.RevokeTokenUseCase;
import org.mitre.authserver.domain.port.out.AccessTokenRepository;
import org.mitre.authserver.domain.port.out.DomainEventPublisher;
import org.mitre.authserver.domain.port.out.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TokenRevocationService implements RevokeTokenUseCase {

    private final AccessTokenRepository accessTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final DomainEventPublisher domainEventPublisher;

    public TokenRevocationService(AccessTokenRepository accessTokenRepository,
                                  RefreshTokenRepository refreshTokenRepository,
                                  DomainEventPublisher domainEventPublisher) {
        this.accessTokenRepository = accessTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public void revoke(String token, String tokenTypeHint, String clientId, String traceId) {
        // Try access token first (or based on hint)
        if (!"refresh_token".equals(tokenTypeHint)) {
            Optional<AccessToken> accessToken = accessTokenRepository.findByTokenValue(token);
            if (accessToken.isPresent()) {
                AccessToken at = accessToken.get();
                domainEventPublisher.publish(at.revoke());
                at.pullDomainEvents(); // clear events already published
                accessTokenRepository.deleteById(at.getId());
                return;
            }
        }

        // Try refresh token
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByTokenValue(token);
        if (refreshToken.isPresent()) {
            RefreshToken rt = refreshToken.get();
            refreshTokenRepository.deleteById(rt.getId());
            return;
        }

        // If access_token hint but not found, try refresh token
        if (!"refresh_token".equals(tokenTypeHint)) {
            // already tried both, nothing to do — RFC 7009 says return 200 anyway
        }
    }
}
