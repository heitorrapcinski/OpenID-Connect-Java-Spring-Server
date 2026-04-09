package org.mitre.oidcprovider.infrastructure.adapter.out.persistence.document;

import org.mitre.oidcprovider.domain.model.WhitelistedSite;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document("whitelisted_sites")
public class WhitelistedSiteDocument {

    @Id
    private String id;

    private String creatorUserId;

    @Indexed
    private String clientId;

    private Set<String> allowedScopes;

    public WhitelistedSiteDocument() {}

    public static WhitelistedSiteDocument fromDomain(WhitelistedSite site) {
        WhitelistedSiteDocument doc = new WhitelistedSiteDocument();
        doc.id = site.getId();
        doc.creatorUserId = site.getCreatorUserId();
        doc.clientId = site.getClientId();
        doc.allowedScopes = site.getAllowedScopes();
        return doc;
    }

    public WhitelistedSite toDomain() {
        return WhitelistedSite.reconstitute(id, creatorUserId, clientId, allowedScopes);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCreatorUserId() { return creatorUserId; }
    public void setCreatorUserId(String creatorUserId) { this.creatorUserId = creatorUserId; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public Set<String> getAllowedScopes() { return allowedScopes; }
    public void setAllowedScopes(Set<String> allowedScopes) { this.allowedScopes = allowedScopes; }
}
