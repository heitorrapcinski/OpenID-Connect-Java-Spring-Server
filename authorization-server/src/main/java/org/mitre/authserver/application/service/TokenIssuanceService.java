package org.mitre.authserver.application.service;

import org.mitre.authserver.domain.event.AccessTokenIssued;
import org.mitre.authserver.domain.event.RefreshTokenIssued;
import org.mitre.authserver.domain.exception.ClientNotFoundException;
import org.mitre.authserver.domain.exception.InvalidGrantException;
import org.mitre.authserver.domain.exception.InvalidScopeException;
import org.mitre.authserver.domain.exception.TokenExpiredException;
import org.mitre.authserver.domain.model.AccessToken;
import org.mitre.authserver.domain.model.AuthorizationCode;
import org.mitre.authserver.domain.model.DeviceCode;
import org.mitre.authserver.domain.model.RefreshToken;
import org.mitre.authserver.domain.model.vo.AuthenticationHolder;
import org.mitre.authserver.domain.model.vo.ClientId;
import org.mitre.authserver.domain.model.vo.PKCEChallenge;
import org.mitre.authserver.domain.model.vo.Scope;
import org.mitre.authserver.domain.model.vo.Subject;
import org.mitre.authserver.domain.model.vo.TokenValue;
import org.mitre.authserver.domain.port.in.IssueTokenUseCase;
import org.mitre.authserver.domain.port.out.AccessTokenRepository;
import org.mitre.authserver.domain.port.out.AuthorizationCodeRepository;
import org.mitre.authserver.domain.port.out.ClientQueryPort;
import org.mitre.authserver.domain.port.out.DeviceCodeRepository;
import org.mitre.authserver.domain.port.out.DomainEventPublisher;
import org.mitre.authserver.domain.port.out.RefreshTokenRepository;
import org.mitre.authserver.domain.port.out.ScopeQueryPort;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TokenIssuanceService implements IssueTokenUseCase {

    private final AccessTokenRepository accessTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthorizationCodeRepository authorizationCodeRepository;
    private final DeviceCodeRepository deviceCodeRepository;
    private final ClientQueryPort clientQueryPort;
    private final ScopeQueryPort scopeQueryPort;
    private final DomainEventPublisher domainEventPublisher;
    private final TokenSerializer tokenSerializer;
    private final Clock clock;

    public TokenIssuanceService(AccessTokenRepository accessTokenRepository,
                                RefreshTokenRepository refreshTokenRepository,
                                AuthorizationCodeRepository authorizationCodeRepository,
                                DeviceCodeRepository deviceCodeRepository,
                                ClientQueryPort clientQueryPort,
                                ScopeQueryPort scopeQueryPort,
                                DomainEventPublisher domainEventPublisher,
                                TokenSerializer tokenSerializer,
                                Clock clock) {
        this.accessTokenRepository = accessTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.authorizationCodeRepository = authorizationCodeRepository;
        this.deviceCodeRepository = deviceCodeRepository;
        this.clientQueryPort = clientQueryPort;
        this.scopeQueryPort = scopeQueryPort;
        this.domainEventPublisher = domainEventPublisher;
        this.tokenSerializer = tokenSerializer;
        this.clock = clock;
    }

    @Override
    public TokenResponse issueFromAuthorizationCode(IssueFromCodeCommand command) {
        AuthorizationCode authCode = authorizationCodeRepository.findByCode(command.code())
                .orElseThrow(() -> new InvalidGrantException("Authorization code not found: " + command.code()));

        Instant now = clock.instant();
        if (authCode.isExpired(now)) {
            throw new TokenExpiredException("Authorization code has expired");
        }

        // Validate PKCE if present
        PKCEChallenge pkce = authCode.getPkceChallenge();
        if (pkce != null) {
            validatePkce(pkce, command.codeVerifier());
        }

        ClientQueryPort.ClientInfo client = clientQueryPort.findById(command.clientId())
                .orElseThrow(() -> new ClientNotFoundException("Client not found: " + command.clientId()));

        // Consume the code (throws if already used)
        authCode.consume();
        authorizationCodeRepository.save(authCode);

        AuthenticationHolder holder = authCode.getAuthenticationHolder();
        Scope scope = Scope.of(holder.scope() != null ? holder.scope() : Collections.emptySet());
        Subject subject = holder.userSub() != null ? new Subject(holder.userSub()) : null;
        ClientId clientId = new ClientId(command.clientId());

        int accessTokenValidity = client.accessTokenValiditySeconds() != null
                ? client.accessTokenValiditySeconds() : 3600;
        Instant accessTokenExpiry = now.plusSeconds(accessTokenValidity);

        AccessToken accessToken = AccessToken.issue(clientId, subject, scope, accessTokenExpiry, holder,
                UUID.randomUUID().toString());
        String accessTokenJwt = tokenSerializer.serialize(accessToken);
        accessToken.setTokenValue(new TokenValue(accessTokenJwt));
        AccessToken savedAccessToken = accessTokenRepository.save(accessToken);

        // Publish access token events
        savedAccessToken.pullDomainEvents().forEach(domainEventPublisher::publish);

        // Issue refresh token if client supports it
        String refreshTokenJwt = null;
        if (client.grantTypes().contains("refresh_token")) {
            int refreshValidity = client.refreshTokenValiditySeconds() != null
                    ? client.refreshTokenValiditySeconds() : 86400;
            Instant refreshExpiry = now.plusSeconds(refreshValidity);
            RefreshToken refreshToken = RefreshToken.issue(clientId, subject, refreshExpiry, holder,
                    UUID.randomUUID().toString());
            refreshTokenJwt = tokenSerializer.serialize(refreshToken);
            refreshToken.setTokenValue(new TokenValue(refreshTokenJwt));
            RefreshToken savedRefresh = refreshTokenRepository.save(refreshToken);

            domainEventPublisher.publish(new RefreshTokenIssued(
                    UUID.randomUUID().toString(), "RefreshTokenIssued", savedRefresh.getId(),
                    now, "authorization-server", command.traceId(),
                    new RefreshTokenIssued.RefreshTokenIssuedPayload(
                            savedRefresh.getId(), command.clientId(),
                            subject != null ? subject.value() : null)
            ));
        }

        return new TokenResponse(
                accessTokenJwt, "Bearer", accessTokenValidity,
                refreshTokenJwt,
                scope.values().stream().sorted().collect(Collectors.joining(" ")),
                null
        );
    }

    @Override
    public TokenResponse issueFromClientCredentials(IssueFromClientCredentialsCommand command) {
        ClientQueryPort.ClientInfo client = clientQueryPort.findById(command.clientId())
                .orElseThrow(() -> new ClientNotFoundException("Client not found: " + command.clientId()));

        if (!client.grantTypes().contains("client_credentials")) {
            throw new InvalidGrantException("Client does not support client_credentials grant");
        }

        Set<String> requestedScope = command.scope() != null ? command.scope() : client.scope();
        validateScopes(requestedScope, client.scope());

        Instant now = clock.instant();
        int validity = client.accessTokenValiditySeconds() != null ? client.accessTokenValiditySeconds() : 3600;
        Instant expiry = now.plusSeconds(validity);

        ClientId clientId = new ClientId(command.clientId());
        Scope scope = Scope.of(requestedScope);
        AuthenticationHolder holder = new AuthenticationHolder(
                command.clientId(), null, true, null,
                requestedScope, Collections.emptySet(), Collections.emptyMap(), Collections.emptySet()
        );

        AccessToken accessToken = AccessToken.issue(clientId, null, scope, expiry, holder,
                UUID.randomUUID().toString());
        String jwt = tokenSerializer.serialize(accessToken);
        accessToken.setTokenValue(new TokenValue(jwt));
        AccessToken saved = accessTokenRepository.save(accessToken);
        saved.pullDomainEvents().forEach(domainEventPublisher::publish);

        return new TokenResponse(
                jwt, "Bearer", validity, null,
                requestedScope.stream().sorted().collect(Collectors.joining(" ")),
                null
        );
    }

    @Override
    public TokenResponse issueFromRefreshToken(IssueFromRefreshTokenCommand command) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenValue(command.refreshToken())
                .orElseThrow(() -> new InvalidGrantException("Refresh token not found"));

        Instant now = clock.instant();
        if (refreshToken.isExpired(now)) {
            throw new TokenExpiredException("Refresh token has expired");
        }

        // use() throws InvalidGrantException if already used
        refreshToken.use();
        refreshTokenRepository.save(refreshToken);
        refreshToken.pullDomainEvents().forEach(domainEventPublisher::publish);

        ClientQueryPort.ClientInfo client = clientQueryPort.findById(command.clientId())
                .orElseThrow(() -> new ClientNotFoundException("Client not found: " + command.clientId()));

        AuthenticationHolder holder = refreshToken.getAuthenticationHolder();
        Set<String> scopeValues = holder != null && holder.scope() != null
                ? holder.scope() : Collections.emptySet();
        Scope scope = Scope.of(scopeValues);
        Subject subject = refreshToken.getSubject();
        ClientId clientId = new ClientId(command.clientId());

        int validity = client.accessTokenValiditySeconds() != null ? client.accessTokenValiditySeconds() : 3600;
        Instant expiry = now.plusSeconds(validity);

        AccessToken newAccessToken = AccessToken.issue(clientId, subject, scope, expiry, holder,
                UUID.randomUUID().toString());
        String accessJwt = tokenSerializer.serialize(newAccessToken);
        newAccessToken.setTokenValue(new TokenValue(accessJwt));
        AccessToken savedAccess = accessTokenRepository.save(newAccessToken);
        savedAccess.pullDomainEvents().forEach(domainEventPublisher::publish);

        String newRefreshJwt = null;
        if (!client.reuseRefreshToken()) {
            int refreshValidity = client.refreshTokenValiditySeconds() != null
                    ? client.refreshTokenValiditySeconds() : 86400;
            Instant refreshExpiry = now.plusSeconds(refreshValidity);
            RefreshToken newRefresh = RefreshToken.issue(clientId, subject, refreshExpiry, holder,
                    UUID.randomUUID().toString());
            newRefreshJwt = tokenSerializer.serialize(newRefresh);
            newRefresh.setTokenValue(new TokenValue(newRefreshJwt));
            RefreshToken savedRefresh = refreshTokenRepository.save(newRefresh);
            domainEventPublisher.publish(new RefreshTokenIssued(
                    UUID.randomUUID().toString(), "RefreshTokenIssued", savedRefresh.getId(),
                    now, "authorization-server", command.traceId(),
                    new RefreshTokenIssued.RefreshTokenIssuedPayload(
                            savedRefresh.getId(), command.clientId(),
                            subject != null ? subject.value() : null)
            ));
        }

        return new TokenResponse(
                accessJwt, "Bearer", validity, newRefreshJwt,
                scopeValues.stream().sorted().collect(Collectors.joining(" ")),
                null
        );
    }

    @Override
    public TokenResponse issueFromDeviceCode(IssueFromDeviceCodeCommand command) {
        DeviceCode deviceCode = deviceCodeRepository.findByDeviceCode(command.deviceCode())
                .orElseThrow(() -> new InvalidGrantException("Device code not found"));

        Instant now = clock.instant();
        if (deviceCode.isExpired(now)) {
            throw new TokenExpiredException("Device code has expired");
        }

        if (!DeviceCode.STATUS_APPROVED.equals(deviceCode.getStatus())) {
            throw new InvalidGrantException("Device code not yet approved, status: " + deviceCode.getStatus());
        }

        ClientQueryPort.ClientInfo client = clientQueryPort.findById(command.clientId())
                .orElseThrow(() -> new ClientNotFoundException("Client not found: " + command.clientId()));

        AuthenticationHolder holder = deviceCode.getAuthenticationHolder();
        Scope scope = deviceCode.getScope();
        Subject subject = holder != null && holder.userSub() != null ? new Subject(holder.userSub()) : null;
        ClientId clientId = new ClientId(command.clientId());

        int validity = client.accessTokenValiditySeconds() != null ? client.accessTokenValiditySeconds() : 3600;
        Instant expiry = now.plusSeconds(validity);

        AccessToken accessToken = AccessToken.issue(clientId, subject, scope, expiry, holder,
                UUID.randomUUID().toString());
        String jwt = tokenSerializer.serialize(accessToken);
        accessToken.setTokenValue(new TokenValue(jwt));
        AccessToken saved = accessTokenRepository.save(accessToken);
        saved.pullDomainEvents().forEach(domainEventPublisher::publish);

        deviceCodeRepository.deleteById(deviceCode.getId());

        return new TokenResponse(
                jwt, "Bearer", validity, null,
                scope.values().stream().sorted().collect(Collectors.joining(" ")),
                null
        );
    }

    private void validatePkce(PKCEChallenge pkce, String codeVerifier) {
        if (codeVerifier == null || codeVerifier.isBlank()) {
            throw new InvalidGrantException("PKCE code_verifier is required");
        }
        if ("S256".equals(pkce.method())) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
                String computed = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
                if (!computed.equals(pkce.challenge())) {
                    throw new InvalidGrantException("PKCE code_verifier does not match code_challenge");
                }
            } catch (InvalidGrantException e) {
                throw e;
            } catch (Exception e) {
                throw new InvalidGrantException("PKCE validation failed: " + e.getMessage());
            }
        } else if ("plain".equals(pkce.method())) {
            if (!codeVerifier.equals(pkce.challenge())) {
                throw new InvalidGrantException("PKCE code_verifier does not match code_challenge");
            }
        }
    }

    private void validateScopes(Set<String> requested, Set<String> allowed) {
        for (String s : requested) {
            if (!allowed.contains(s)) {
                throw new InvalidScopeException("Scope not allowed for client: " + s);
            }
            if (scopeQueryPort.isRestricted(s)) {
                throw new InvalidScopeException("Scope is restricted: " + s);
            }
        }
    }
}
