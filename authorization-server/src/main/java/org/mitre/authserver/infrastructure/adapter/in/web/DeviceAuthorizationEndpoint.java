package org.mitre.authserver.infrastructure.adapter.in.web;

import org.mitre.authserver.domain.port.in.DeviceAuthorizationUseCase;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/device_authorization")
public class DeviceAuthorizationEndpoint {

    private final DeviceAuthorizationUseCase deviceAuthorizationUseCase;

    public DeviceAuthorizationEndpoint(DeviceAuthorizationUseCase deviceAuthorizationUseCase) {
        this.deviceAuthorizationUseCase = deviceAuthorizationUseCase;
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> deviceAuthorization(
            @RequestParam("client_id") String clientId,
            @RequestParam(value = "scope", required = false) String scope) {

        DeviceAuthorizationUseCase.DeviceAuthCommand command =
                new DeviceAuthorizationUseCase.DeviceAuthCommand(clientId, scope, null);

        DeviceAuthorizationUseCase.DeviceAuthResult result =
                deviceAuthorizationUseCase.initiateDeviceAuthorization(command);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("device_code", result.deviceCode());
        response.put("user_code", result.userCode());
        response.put("verification_uri", result.verificationUri());
        response.put("expires_in", result.expiresIn());
        response.put("interval", result.interval());

        return ResponseEntity.ok(response);
    }
}
