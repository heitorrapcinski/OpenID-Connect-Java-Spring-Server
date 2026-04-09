package org.mitre.authserver.domain.port.in;

import java.util.Set;

public interface IssueTokenUseCase {

    TokenResponse issueFromAuthorizationCode(IssueFromCodeCommand command);

    TokenResponse issueFromClientCredentials(IssueFromClientCredentialsCommand command);

    TokenResponse issueFromRefreshToken(IssueFromRefreshTokenCommand command);

    TokenResponse issueFromDeviceCode(IssueFromDeviceCodeCommand command);

    record IssueFromCodeCommand(
            String clientId,
            String code,
            String redirectUri,
            String codeVerifier,
            String traceId
    ) {}

    record IssueFromClientCredentialsCommand(
            String clientId,
            Set<String> scope,
            String traceId
    ) {}

    record IssueFromRefreshTokenCommand(
            String clientId,
            String refreshToken,
            Set<String> scope,
            String traceId
    ) {}

    record IssueFromDeviceCodeCommand(
            String clientId,
            String deviceCode,
            String traceId
    ) {}

    record TokenResponse(
            String accessToken,
            String tokenType,
            long expiresIn,
            String refreshToken,
            String scope,
            String idToken
    ) {}
}
