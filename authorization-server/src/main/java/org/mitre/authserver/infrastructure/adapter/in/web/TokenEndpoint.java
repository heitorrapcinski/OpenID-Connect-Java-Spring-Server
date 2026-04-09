package org.mitre.authserver.infrastructure.adapter.in.web;

import org.mitre.authserver.domain.port.in.IssueTokenUseCase;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/token")
public class TokenEndpoint {

    private final IssueTokenUseCase issueTokenUseCase;

    public TokenEndpoint(IssueTokenUseCase issueTokenUseCase) {
        this.issueTokenUseCase = issueTokenUseCase;
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> token(
            @RequestParam("grant_type") String grantType,
            @RequestParam(value = "client_id", required = false) String clientId,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @RequestParam(value = "code_verifier", required = false) String codeVerifier,
            @RequestParam(value = "refresh_token", required = false) String refreshToken,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "device_code", required = false) String deviceCode) {

        IssueTokenUseCase.TokenResponse response = switch (grantType) {
            case "authorization_code" -> issueTokenUseCase.issueFromAuthorizationCode(
                    new IssueTokenUseCase.IssueFromCodeCommand(clientId, code, redirectUri, codeVerifier, null));
            case "client_credentials" -> issueTokenUseCase.issueFromClientCredentials(
                    new IssueTokenUseCase.IssueFromClientCredentialsCommand(clientId, parseScope(scope), null));
            case "refresh_token" -> issueTokenUseCase.issueFromRefreshToken(
                    new IssueTokenUseCase.IssueFromRefreshTokenCommand(clientId, refreshToken, parseScope(scope), null));
            case "urn:ietf:params:oauth:grant-type:device_code" -> issueTokenUseCase.issueFromDeviceCode(
                    new IssueTokenUseCase.IssueFromDeviceCodeCommand(clientId, deviceCode, null));
            default -> throw new IllegalArgumentException("Unsupported grant_type: " + grantType);
        };

        return ResponseEntity.ok(buildTokenResponse(response));
    }

    private Map<String, Object> buildTokenResponse(IssueTokenUseCase.TokenResponse response) {
        var map = new java.util.LinkedHashMap<String, Object>();
        map.put("access_token", response.accessToken());
        map.put("token_type", response.tokenType());
        map.put("expires_in", response.expiresIn());
        if (response.refreshToken() != null) map.put("refresh_token", response.refreshToken());
        if (response.scope() != null) map.put("scope", response.scope());
        if (response.idToken() != null) map.put("id_token", response.idToken());
        return map;
    }

    private Set<String> parseScope(String scope) {
        if (scope == null || scope.isBlank()) return Collections.emptySet();
        return new HashSet<>(Arrays.asList(scope.trim().split("\\s+")));
    }
}
