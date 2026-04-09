package org.mitre.authserver.infrastructure.adapter.out.persistence.document;

import org.mitre.authserver.domain.model.DeviceCode;
import org.mitre.authserver.domain.model.vo.ClientId;
import org.mitre.authserver.domain.model.vo.DeviceCodeValue;
import org.mitre.authserver.domain.model.vo.Scope;
import org.mitre.authserver.domain.model.vo.UserCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Document("device_codes")
public class DeviceCodeDocument {

    @Id
    private String id;

    @Indexed
    private String deviceCode;

    @Indexed(unique = true)
    private String userCode;

    private String clientId;
    private Set<String> scope;

    @Indexed(expireAfterSeconds = 0)
    private Instant expiration;

    private String status;
    private Map<String, String> requestParameters;
    private AuthenticationHolderDocument authenticationHolder;

    @Version
    private Long version;

    public DeviceCodeDocument() {}

    public static DeviceCodeDocument fromDomain(DeviceCode dc) {
        DeviceCodeDocument doc = new DeviceCodeDocument();
        doc.id = dc.getId();
        doc.deviceCode = dc.getDeviceCodeValue().value();
        doc.userCode = dc.getUserCode().value();
        doc.clientId = dc.getClientId() != null ? dc.getClientId().value() : null;
        doc.scope = dc.getScope().values();
        doc.expiration = dc.getExpiration();
        doc.status = dc.getStatus();
        doc.requestParameters = dc.getRequestParameters();
        doc.authenticationHolder = AuthenticationHolderDocument.fromDomain(dc.getAuthenticationHolder());
        return doc;
    }

    public DeviceCode toDomain() {
        return DeviceCode.reconstitute(
                id,
                new DeviceCodeValue(deviceCode),
                new UserCode(userCode),
                clientId != null ? new ClientId(clientId) : null,
                Scope.of(scope != null ? scope : Set.of()),
                expiration,
                status,
                authenticationHolder != null ? authenticationHolder.toDomain() : null,
                requestParameters
        );
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDeviceCode() { return deviceCode; }
    public void setDeviceCode(String deviceCode) { this.deviceCode = deviceCode; }
    public String getUserCode() { return userCode; }
    public void setUserCode(String userCode) { this.userCode = userCode; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public Set<String> getScope() { return scope; }
    public void setScope(Set<String> scope) { this.scope = scope; }
    public Instant getExpiration() { return expiration; }
    public void setExpiration(Instant expiration) { this.expiration = expiration; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Map<String, String> getRequestParameters() { return requestParameters; }
    public void setRequestParameters(Map<String, String> requestParameters) { this.requestParameters = requestParameters; }
    public AuthenticationHolderDocument getAuthenticationHolder() { return authenticationHolder; }
    public void setAuthenticationHolder(AuthenticationHolderDocument authenticationHolder) { this.authenticationHolder = authenticationHolder; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
