package org.mitre.authserver.application.service;

import org.mitre.authserver.domain.exception.ClientNotFoundException;
import org.mitre.authserver.domain.exception.InvalidGrantException;
import org.mitre.authserver.domain.exception.InvalidScopeException;
import org.mitre.authserver.domain.model.AuthorizationCode;
import org.mitre.authserver.domain.model.vo.AuthenticationHolder;
import org.mitre.authserver.domain.model.vo.ClientId;
import org.mitre.authserver.domain.model.vo.PKCEChallenge;
import org.mitre.authserver.domain.port.in.AuthorizeUseCase;
import org.mitre.authserver.domain.port.out.AuthorizationCodeRepository;
import org.mitre.authserver.domain.port.out.ClientQueryPort;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthorizationService implements AuthorizeUseCase {

    private final AuthorizationCodeRepository authorizationCodeRepository;
    private final ClientQueryPort clientQueryPort;
    private final Clock clock;

    public AuthorizationService(AuthorizationCodeRepository authorizationCodeRepository,
                                ClientQueryPort clientQueryPort,
                                Clock clock) {
        this.authorizationCodeRepository = authorizationCodeRepository;
        this.clientQueryPort = clientQueryPort;
        this.clock = clock;
    }

    @Override
    public AuthorizeResult authorize(AuthorizeCommand command) {
        ClientQueryPort.ClientInfo client = clientQueryPort.findById(command.clientId())
                .orElseThrow(() -> new ClientNotFoundException("Client not found: " + command.clientId()));

        // Validate redirect_uri
        if (command.redirectUri() != null && !client.redirectUris().contains(command.redirectUri())) {
            throw new InvalidGrantException("Invalid redirect_uri: " + command.redirectUri());
        }

        // Validate scopes
        Set<String> requestedScopes = parseScope(command.scope());
        for (String s : requestedScopes) {
            if (!client.scope().contains(s)) {
                throw new InvalidScopeException("Scope not allowed for client: " + s);
            }
        }

        // Build PKCE challenge if provided
        PKCEChallenge pkceChallenge = null;
        if (command.codeChallenge() != null && !command.codeChallenge().isBlank()) {
            String method = command.codeChallengeMethod() != null ? command.codeChallengeMethod() : "plain";
            pkceChallenge = new PKCEChallenge(command.codeChallenge(), method);
        }

        AuthenticationHolder holder = new AuthenticationHolder(
                command.clientId(),
                command.userSub(),
                true,
                command.redirectUri(),
                requestedScopes,
                new HashSet<>(Collections.singletonList("code")),
                Collections.emptyMap(),
                Collections.emptySet()
        );

        String code = UUID.randomUUID().toString().replace("-", "");
        Instant expiry = clock.instant().plusSeconds(600); // 10 minutes

        AuthorizationCode authCode = AuthorizationCode.create(
                code,
                new ClientId(command.clientId()),
                holder,
                expiry,
                pkceChallenge
        );

        authorizationCodeRepository.save(authCode);

        return new AuthorizeResult(code, command.state());
    }

    private Set<String> parseScope(String scope) {
        if (scope == null || scope.isBlank()) return Collections.emptySet();
        return new HashSet<>(Arrays.asList(scope.trim().split("\\s+")));
    }
}
