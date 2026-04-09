package org.mitre.oidcprovider.infrastructure.adapter.out.persistence.document;

import org.mitre.oidcprovider.domain.model.ApprovedSite;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Document("approved_sites")
@CompoundIndexes({
    @CompoundIndex(name = "idx_user_client", def = "{'userId': 1, 'clientId': 1}"),
    @CompoundIndex(name = "idx_client_id", def = "{'clientId': 1}")
})
public class ApprovedSiteDocument {

    @Id
    private String id;

    private String userId;
    private String clientId;
    private Instant creationDate;
    private Instant accessDate;
    private Instant timeoutDate;
    private Set<String> allowedScopes;

    @Version
    private Long version;

    public ApprovedSiteDocument() {}

    public static ApprovedSiteDocument fromDomain(ApprovedSite site) {
        ApprovedSiteDocument doc = new ApprovedSiteDocument();
        doc.id = site.getId();
        doc.userId = site.getUserId();
        doc.clientId = site.getClientId();
        doc.creationDate = site.getCreationDate();
        doc.accessDate = site.getAccessDate();
        doc.timeoutDate = site.getTimeoutDate();
        doc.allowedScopes = site.getAllowedScopes();
        return doc;
    }

    public ApprovedSite toDomain() {
        return ApprovedSite.reconstitute(id, userId, clientId, creationDate, accessDate, timeoutDate, allowedScopes);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public Instant getCreationDate() { return creationDate; }
    public void setCreationDate(Instant creationDate) { this.creationDate = creationDate; }
    public Instant getAccessDate() { return accessDate; }
    public void setAccessDate(Instant accessDate) { this.accessDate = accessDate; }
    public Instant getTimeoutDate() { return timeoutDate; }
    public void setTimeoutDate(Instant timeoutDate) { this.timeoutDate = timeoutDate; }
    public Set<String> getAllowedScopes() { return allowedScopes; }
    public void setAllowedScopes(Set<String> allowedScopes) { this.allowedScopes = allowedScopes; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
