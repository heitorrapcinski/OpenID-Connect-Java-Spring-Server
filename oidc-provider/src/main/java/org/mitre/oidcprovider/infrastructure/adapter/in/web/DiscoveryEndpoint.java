package org.mitre.oidcprovider.infrastructure.adapter.in.web;

import org.mitre.oidcprovider.domain.port.in.GetDiscoveryDocumentUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/.well-known/openid-configuration")
public class DiscoveryEndpoint {

    private final GetDiscoveryDocumentUseCase discoveryUseCase;
    private final String issuerUrl;

    public DiscoveryEndpoint(
            GetDiscoveryDocumentUseCase discoveryUseCase,
            @Value("${server.issuer-url:http://localhost:8082}") String issuerUrl) {
        this.discoveryUseCase = discoveryUseCase;
        this.issuerUrl = issuerUrl;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDiscoveryDocument() {
        return ResponseEntity.ok(discoveryUseCase.getDiscoveryDocument(issuerUrl));
    }
}
