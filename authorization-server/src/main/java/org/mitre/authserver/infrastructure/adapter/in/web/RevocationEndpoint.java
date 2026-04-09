package org.mitre.authserver.infrastructure.adapter.in.web;

import org.mitre.authserver.domain.port.in.RevokeTokenUseCase;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/revoke")
public class RevocationEndpoint {

    private final RevokeTokenUseCase revokeTokenUseCase;

    public RevocationEndpoint(RevokeTokenUseCase revokeTokenUseCase) {
        this.revokeTokenUseCase = revokeTokenUseCase;
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> revoke(
            @RequestParam("token") String token,
            @RequestParam(value = "token_type_hint", required = false) String tokenTypeHint,
            @RequestParam(value = "client_id", required = false) String clientId) {

        revokeTokenUseCase.revoke(token, tokenTypeHint, clientId, null);
        // RFC 7009: always return 200
        return ResponseEntity.ok().build();
    }
}
