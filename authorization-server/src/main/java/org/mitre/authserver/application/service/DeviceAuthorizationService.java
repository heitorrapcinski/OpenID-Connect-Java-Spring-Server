package org.mitre.authserver.application.service;

import org.mitre.authserver.domain.exception.ClientNotFoundException;
import org.mitre.authserver.domain.model.DeviceCode;
import org.mitre.authserver.domain.model.vo.ClientId;
import org.mitre.authserver.domain.model.vo.DeviceCodeValue;
import org.mitre.authserver.domain.model.vo.Scope;
import org.mitre.authserver.domain.model.vo.UserCode;
import org.mitre.authserver.domain.port.in.DeviceAuthorizationUseCase;
import org.mitre.authserver.domain.port.out.ClientQueryPort;
import org.mitre.authserver.domain.port.out.DeviceCodeRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Service
public class DeviceAuthorizationService implements DeviceAuthorizationUseCase {

    private static final String VERIFICATION_URI = "http://localhost:8080/device";
    private static final int DEVICE_CODE_EXPIRY_SECONDS = 600;
    private static final int POLLING_INTERVAL_SECONDS = 5;
    private static final String USER_CODE_CHARS = "BCDFGHJKLMNPQRSTVWXZ";

    private final DeviceCodeRepository deviceCodeRepository;
    private final ClientQueryPort clientQueryPort;
    private final Clock clock;

    public DeviceAuthorizationService(DeviceCodeRepository deviceCodeRepository,
                                      ClientQueryPort clientQueryPort,
                                      Clock clock) {
        this.deviceCodeRepository = deviceCodeRepository;
        this.clientQueryPort = clientQueryPort;
        this.clock = clock;
    }

    @Override
    public DeviceAuthResult initiateDeviceAuthorization(DeviceAuthCommand command) {
        clientQueryPort.findById(command.clientId())
                .orElseThrow(() -> new ClientNotFoundException("Client not found: " + command.clientId()));

        Set<String> scopeValues = parseScope(command.scope());
        Scope scope = Scope.of(scopeValues);

        String deviceCodeStr = UUID.randomUUID().toString();
        String userCodeStr = generateUserCode();

        Instant expiry = clock.instant().plusSeconds(DEVICE_CODE_EXPIRY_SECONDS);

        DeviceCode deviceCode = DeviceCode.create(
                new DeviceCodeValue(deviceCodeStr),
                new UserCode(userCodeStr),
                new ClientId(command.clientId()),
                scope,
                expiry,
                Map.of("scope", command.scope() != null ? command.scope() : "")
        );

        deviceCodeRepository.save(deviceCode);

        return new DeviceAuthResult(
                deviceCodeStr,
                userCodeStr,
                VERIFICATION_URI,
                DEVICE_CODE_EXPIRY_SECONDS,
                POLLING_INTERVAL_SECONDS
        );
    }

    /**
     * Generates an 8-character user code in format "XXXX-XXXX" using consonants only
     * (easier to read/type, avoids ambiguous characters).
     */
    private String generateUserCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(USER_CODE_CHARS.charAt(random.nextInt(USER_CODE_CHARS.length())));
        }
        sb.append('-');
        for (int i = 0; i < 4; i++) {
            sb.append(USER_CODE_CHARS.charAt(random.nextInt(USER_CODE_CHARS.length())));
        }
        return sb.toString();
    }

    private Set<String> parseScope(String scope) {
        if (scope == null || scope.isBlank()) return Collections.emptySet();
        return new HashSet<>(Arrays.asList(scope.trim().split("\\s+")));
    }
}
