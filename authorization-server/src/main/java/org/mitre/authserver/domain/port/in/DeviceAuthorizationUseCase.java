package org.mitre.authserver.domain.port.in;

public interface DeviceAuthorizationUseCase {

    DeviceAuthResult initiateDeviceAuthorization(DeviceAuthCommand command);

    record DeviceAuthCommand(
            String clientId,
            String scope,
            String traceId
    ) {}

    record DeviceAuthResult(
            String deviceCode,
            String userCode,
            String verificationUri,
            int expiresIn,
            int interval
    ) {}
}
