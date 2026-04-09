package org.mitre.authserver.infrastructure.adapter.in.web;

import org.mitre.authserver.domain.port.in.IntrospectTokenUseCase;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/introspect")
public class IntrospectionEndpoint {

    private final IntrospectTokenUseCase introspectTokenUseCase;

    public IntrospectionEndpoint(IntrospectTokenUseCase introspectTokenUseCase) {
        this.introspectTokenUseCase = introspectTokenUseCase;
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> introspect(
            @RequestParam("token") String token,
            @RequestParam(value = "client_id", required = false) String clientId) {

        IntrospectTokenUseCase.IntrospectionResult result = introspectTokenUseCase.introspect(token, clientId);
        return ResponseEntity.ok(buildResponse(result));
    }

    private Map<String, Object> buildResponse(IntrospectTokenUseCase.IntrospectionResult result) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("active", result.active());
        if (result.active()) {
            if (result.sub() != null) map.put("sub", result.sub());
            if (result.clientId() != null) map.put("client_id", result.clientId());
            if (result.scope() != null) map.put("scope", result.scope());
            if (result.exp() != null) map.put("exp", result.exp());
            if (result.iat() != null) map.put("iat", result.iat());
            if (result.tokenType() != null) map.put("token_type", result.tokenType());
        }
        return map;
    }
}
