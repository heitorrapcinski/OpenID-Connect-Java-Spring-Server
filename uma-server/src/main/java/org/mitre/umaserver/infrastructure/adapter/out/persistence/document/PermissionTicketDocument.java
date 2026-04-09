package org.mitre.umaserver.infrastructure.adapter.out.persistence.document;

import org.mitre.umaserver.domain.model.PermissionTicket;
import org.mitre.umaserver.domain.model.vo.ClaimsSupplied;
import org.mitre.umaserver.domain.model.vo.Permission;
import org.mitre.umaserver.domain.model.vo.TicketValue;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Document("permission_tickets")
public class PermissionTicketDocument {

    @Id
    private String id;
    @Indexed(unique = true)
    private String ticket;
    @Indexed(expireAfterSeconds = 0)
    private Date expiration;
    private PermissionDocument permission;
    private List<ClaimsSuppliedDocument> claimsSupplied;
    private boolean used;
    @Version
    private Long version;

    public PermissionTicketDocument() {}

    public static PermissionTicketDocument fromDomain(PermissionTicket domain) {
        PermissionTicketDocument doc = new PermissionTicketDocument();
        doc.id = domain.getId();
        doc.ticket = domain.getTicket().value();
        doc.expiration = Date.from(domain.getExpiration());
        doc.permission = PermissionDocument.fromDomain(domain.getPermission());
        doc.claimsSupplied = domain.getClaimsSupplied() != null
                ? domain.getClaimsSupplied().stream().map(ClaimsSuppliedDocument::fromDomain).collect(Collectors.toList())
                : new ArrayList<>();
        doc.used = domain.isUsed();
        return doc;
    }

    public PermissionTicket toDomain() {
        PermissionTicket pt = PermissionTicket.create(
                new TicketValue(ticket),
                expiration.toInstant(),
                permission.toDomain()
        );
        pt.setId(id);
        if (claimsSupplied != null) {
            pt.setClaimsSupplied(claimsSupplied.stream().map(ClaimsSuppliedDocument::toDomain).collect(Collectors.toList()));
        }
        if (used) pt.markUsed();
        return pt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTicket() { return ticket; }
    public void setTicket(String ticket) { this.ticket = ticket; }
    public Date getExpiration() { return expiration; }
    public void setExpiration(Date expiration) { this.expiration = expiration; }
    public PermissionDocument getPermission() { return permission; }
    public void setPermission(PermissionDocument permission) { this.permission = permission; }
    public List<ClaimsSuppliedDocument> getClaimsSupplied() { return claimsSupplied; }
    public void setClaimsSupplied(List<ClaimsSuppliedDocument> claimsSupplied) { this.claimsSupplied = claimsSupplied; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public static class PermissionDocument {
        private String resourceSetId;
        private Set<String> scopes;

        public PermissionDocument() {}

        public static PermissionDocument fromDomain(Permission permission) {
            PermissionDocument doc = new PermissionDocument();
            doc.resourceSetId = permission.resourceSetId();
            doc.scopes = permission.scopes() != null ? new HashSet<>(permission.scopes()) : new HashSet<>();
            return doc;
        }

        public Permission toDomain() {
            return new Permission(resourceSetId, scopes);
        }

        public String getResourceSetId() { return resourceSetId; }
        public void setResourceSetId(String resourceSetId) { this.resourceSetId = resourceSetId; }
        public Set<String> getScopes() { return scopes; }
        public void setScopes(Set<String> scopes) { this.scopes = scopes; }
    }

    public static class ClaimsSuppliedDocument {
        private String name;
        private String claimType;
        private String value;
        private List<String> claimTokenFormat;
        private List<String> issuer;

        public ClaimsSuppliedDocument() {}

        public static ClaimsSuppliedDocument fromDomain(ClaimsSupplied cs) {
            ClaimsSuppliedDocument doc = new ClaimsSuppliedDocument();
            doc.name = cs.name();
            doc.claimType = cs.claimType();
            doc.value = cs.value();
            doc.claimTokenFormat = cs.claimTokenFormat();
            doc.issuer = cs.issuer();
            return doc;
        }

        public ClaimsSupplied toDomain() {
            return new ClaimsSupplied(name, claimType, value, claimTokenFormat, issuer);
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getClaimType() { return claimType; }
        public void setClaimType(String claimType) { this.claimType = claimType; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public List<String> getClaimTokenFormat() { return claimTokenFormat; }
        public void setClaimTokenFormat(List<String> claimTokenFormat) { this.claimTokenFormat = claimTokenFormat; }
        public List<String> getIssuer() { return issuer; }
        public void setIssuer(List<String> issuer) { this.issuer = issuer; }
    }
}
