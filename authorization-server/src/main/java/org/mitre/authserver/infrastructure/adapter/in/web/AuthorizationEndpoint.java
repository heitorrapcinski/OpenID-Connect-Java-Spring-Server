package org.mitre.authserver.infrastructure.adapter.in.web;

import org.mitre.authserver.domain.port.in.AuthorizeUseCase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/authorize")
public class AuthorizationEndpoint {

    private final AuthorizeUseCase authorizeUseCase;

    public AuthorizationEndpoint(AuthorizeUseCase authorizeUseCase) {
        this.authorizeUseCase = authorizeUseCase;
    }

    @GetMapping
    public ResponseEntity<Void> authorize(
            @RequestParam("response_type") String responseType,
            @RequestParam("client_id") String clientId,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "code_challenge", required = false) String codeChallenge,
            @RequestParam(value = "code_challenge_method", required = false) String codeChallengeMethod,
            @RequestParam(value = "user_sub", required = false) String userSub) {

        return handleAuthorize(responseType, clientId, redirectUri, scope, state,
                codeChallenge, codeChallengeMethod, userSub);
    }

    @PostMapping
    public ResponseEntity<Void> authorizePost(
            @RequestParam("response_type") String responseType,
            @RequestParam("client_id") String clientId,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "code_challenge", required = false) String codeChallenge,
            @RequestParam(value = "code_challenge_method", required = false) String codeChallengeMethod,
            @RequestParam(value = "user_sub", required = false) String userSub) {

        return handleAuthorize(responseType, clientId, redirectUri, scope, state,
                codeChallenge, codeChallengeMethod, userSub);
    }

    private ResponseEntity<Void> handleAuthorize(String responseType, String clientId,
                                                  String redirectUri, String scope, String state,
                                                  String codeChallenge, String codeChallengeMethod,
                                                  String userSub) {
        AuthorizeUseCase.AuthorizeCommand command = new AuthorizeUseCase.AuthorizeCommand(
                clientId, responseType, redirectUri, scope, state,
                codeChallenge, codeChallengeMethod, userSub, null
        );

        AuthorizeUseCase.AuthorizeResult result = authorizeUseCase.authorize(command);

        String location = buildRedirectUri(redirectUri, result.code(), result.state());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(location));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    private String buildRedirectUri(String redirectUri, String code, String state) {
        StringBuilder sb = new StringBuilder(redirectUri != null ? redirectUri : "/");
        sb.append("?code=").append(encode(code));
        if (state != null && !state.isBlank()) {
            sb.append("&state=").append(encode(state));
        }
        return sb.toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
