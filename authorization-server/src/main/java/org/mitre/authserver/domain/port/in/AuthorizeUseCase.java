package org.mitre.authserver.domain.port.in;

public interface AuthorizeUseCase {

    AuthorizeResult authorize(AuthorizeCommand command);

    record AuthorizeCommand(
            String clientId,
            String responseType,
            String redirectUri,
            String scope,
            String state,
            String codeChallenge,
            String codeChallengeMethod,
            String userSub,
            String traceId
    ) {}

    record AuthorizeResult(
            String code,
            String state
    ) {}
}
