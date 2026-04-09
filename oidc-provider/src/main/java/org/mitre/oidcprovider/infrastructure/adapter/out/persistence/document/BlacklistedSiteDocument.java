package org.mitre.oidcprovider.infrastructure.adapter.out.persistence.document;

import org.mitre.oidcprovider.domain.model.BlacklistedSite;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("blacklisted_sites")
public class BlacklistedSiteDocument {

    @Id
    private String id;

    private String uri;

    public BlacklistedSiteDocument() {}

    public static BlacklistedSiteDocument fromDomain(BlacklistedSite site) {
        BlacklistedSiteDocument doc = new BlacklistedSiteDocument();
        doc.id = site.getId();
        doc.uri = site.getUri();
        return doc;
    }

    public BlacklistedSite toDomain() {
        return BlacklistedSite.reconstitute(id, uri);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }
}
