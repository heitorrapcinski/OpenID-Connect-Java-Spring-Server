package org.mitre.authserver.domain.model;

import org.mitre.authserver.domain.model.vo.AuthenticationHolder;
import org.mitre.authserver.domain.model.vo.ClientId;
import org.mitre.authserver.domain.model.vo.DeviceCodeValue;
import org.mitre.authserver.domain.model.vo.Scope;
import org.mitre.authserver.domain.model.vo.UserCode;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Aggregate for device codes (RFC 8628).
 * No framework annotations — pure domain object.
 */
public class DeviceCode {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_DENIED = "DENIED";
    public static final String STATUS_EXPIRED = "EXPIRED";

    private final String id;
    private final DeviceCodeValue deviceCodeValue;
    private final UserCode userCode;
    private final ClientId clientId;
    private final Scope scope;
    private final Instant expiration;
    private String status;
    private AuthenticationHolder authenticationHolder; // nullable
    private final Map<String, String> requestParameters;

    private DeviceCode(String id, DeviceCodeValue deviceCodeValue, UserCode userCode,
                       ClientId clientId, Scope scope, Instant expiration, String status,
                       AuthenticationHolder authenticationHolder, Map<String, String> requestParameters) {
        this.id = id;
        this.deviceCodeValue = deviceCodeValue;
        this.userCode = userCode;
        this.clientId = clientId;
        this.scope = scope;
        this.expiration = expiration;
        this.status = status;
        this.authenticationHolder = authenticationHolder;
        this.requestParameters = requestParameters;
    }

    public static DeviceCode create(DeviceCodeValue deviceCodeValue, UserCode userCode,
                                    ClientId clientId, Scope scope, Instant expiration,
                                    Map<String, String> requestParameters) {
        return new DeviceCode(
                UUID.randomUUID().toString(),
                deviceCodeValue, userCode, clientId, scope, expiration,
                STATUS_PENDING, null, requestParameters
        );
    }

    public static DeviceCode reconstitute(String id, DeviceCodeValue deviceCodeValue, UserCode userCode,
                                          ClientId clientId, Scope scope, Instant expiration,
                                          String status, AuthenticationHolder authenticationHolder,
                                          Map<String, String> requestParameters) {
        return new DeviceCode(id, deviceCodeValue, userCode, clientId, scope, expiration,
                status, authenticationHolder, requestParameters);
    }

    public void approve(AuthenticationHolder authenticationHolder) {
        this.status = STATUS_APPROVED;
        this.authenticationHolder = authenticationHolder;
    }

    public void deny() {
        this.status = STATUS_DENIED;
    }

    public boolean isExpired(Instant now) {
        return expiration.isBefore(now);
    }

    // Getters
    public String getId() { return id; }
    public DeviceCodeValue getDeviceCodeValue() { return deviceCodeValue; }
    public UserCode getUserCode() { return userCode; }
    public ClientId getClientId() { return clientId; }
    public Scope getScope() { return scope; }
    public Instant getExpiration() { return expiration; }
    public String getStatus() { return status; }
    public AuthenticationHolder getAuthenticationHolder() { return authenticationHolder; }
    public Map<String, String> getRequestParameters() { return requestParameters; }
}
