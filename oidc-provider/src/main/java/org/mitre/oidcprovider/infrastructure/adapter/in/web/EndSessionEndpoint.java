package org.mitre.oidcprovider.infrastructure.adapter.in.web;

import org.mitre.oidcprovider.domain.port.in.EndSessionUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/end_session")
public class EndSessionEndpoint {

    private final EndSessionUseCase endSessionUseCase;

    public EndSessionEndpoint(EndSessionUseCase endSessionUseCase) {
        this.endSessionUseCase = endSessionUseCase;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> endSessionGet(
            @RequestParam(value = "id_token_hint", required = false) String idTokenHint,
            @RequestParam(value = "post_logout_redirect_uri", required = false) String postLogoutRedirectUri,
            @RequestParam(value = "state", required = false) String state) {
        return handleEndSession(idTokenHint, postLogoutRedirectUri, state);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> endSessionPost(
            @RequestParam(value = "id_token_hint", required = false) String idTokenHint,
            @RequestParam(value = "post_logout_redirect_uri", required = false) String postLogoutRedirectUri,
            @RequestParam(value = "state", required = false) String state) {
        return handleEndSession(idTokenHint, postLogoutRedirectUri, state);
    }

    private ResponseEntity<Map<String, String>> handleEndSession(
            String idTokenHint, String postLogoutRedirectUri, String state) {
        endSessionUseCase.endSession(idTokenHint, postLogoutRedirectUri, state);
        return ResponseEntity.ok(Map.of("message", "Session ended"));
    }
}
