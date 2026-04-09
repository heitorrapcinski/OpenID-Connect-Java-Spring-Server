package org.mitre.oidcprovider.infrastructure.adapter.in.web;

import org.mitre.oidcprovider.domain.port.in.GetJwksUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/jwks")
public class JwksEndpoint {

    private final GetJwksUseCase getJwksUseCase;

    public JwksEndpoint(GetJwksUseCase getJwksUseCase) {
        this.getJwksUseCase = getJwksUseCase;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getJwks() {
        return ResponseEntity.ok(getJwksUseCase.getJwks());
    }
}
