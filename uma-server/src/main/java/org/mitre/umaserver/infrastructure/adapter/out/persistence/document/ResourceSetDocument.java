package org.mitre.umaserver.infrastructure.adapter.out.persistence.document;

import org.mitre.umaserver.domain.model.ResourceSet;
import org.mitre.umaserver.domain.model.vo.ClientId;
import org.mitre.umaserver.domain.model.vo.Owner;
import org.mitre.umaserver.domain.model.vo.ResourceSetId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Document("resource_sets")
@CompoundIndexes({@CompoundIndex(def = "{'owner': 1, 'clientId': 1}")})
public class ResourceSetDocument {

    @Id
    private String id;
    private String name;
    private String uri;
    private String type;
    private Set<String> scopes;
    private String iconUri;
    @Indexed
    private String owner;
    @Indexed
    private String clientId;
    private List<PolicyDocument> policies;
    @Version
    private Long version;

    public ResourceSetDocument() {}

    public static ResourceSetDocument fromDomain(ResourceSet resourceSet) {
        ResourceSetDocument doc = new ResourceSetDocument();
        doc.id = resourceSet.getId().value();
        doc.name = resourceSet.getName();
        doc.uri = resourceSet.getUri();
        doc.type = resourceSet.getType();
        doc.scopes = resourceSet.getScopes() != null ? new HashSet<>(resourceSet.getScopes()) : new HashSet<>();
        doc.iconUri = resourceSet.getIconUri();
        doc.owner = resourceSet.getOwner().value();
        doc.clientId = resourceSet.getClientId() != null ? resourceSet.getClientId().value() : null;
        doc.policies = resourceSet.getPolicies() != null
                ? resourceSet.getPolicies().stream().map(PolicyDocument::fromDomain).collect(Collectors.toList())
                : new ArrayList<>();
        return doc;
    }

    public ResourceSet toDomain() {
        ResourceSet rs = ResourceSet.create(
                new ResourceSetId(id),
                name,
                new Owner(owner),
                clientId != null ? new ClientId(clientId) : null
        );
        rs.update(name, uri, type, scopes, iconUri);
        if (policies != null) {
            rs.setPolicies(policies.stream().map(PolicyDocument::toDomain).collect(Collectors.toList()));
        }
        return rs;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Set<String> getScopes() { return scopes; }
    public void setScopes(Set<String> scopes) { this.scopes = scopes; }
    public String getIconUri() { return iconUri; }
    public void setIconUri(String iconUri) { this.iconUri = iconUri; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public List<PolicyDocument> getPolicies() { return policies; }
    public void setPolicies(List<PolicyDocument> policies) { this.policies = policies; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public static class PolicyDocument {
        private String name;
        private Set<String> scopes;
        private List<ClaimDocument> claimsRequired;

        public PolicyDocument() {}

        public static PolicyDocument fromDomain(ResourceSet.Policy policy) {
            PolicyDocument doc = new PolicyDocument();
            doc.name = policy.getName();
            doc.scopes = policy.getScopes() != null ? new HashSet<>(policy.getScopes()) : new HashSet<>();
            doc.claimsRequired = policy.getClaimsRequired() != null
                    ? policy.getClaimsRequired().stream().map(ClaimDocument::fromDomain).collect(Collectors.toList())
                    : new ArrayList<>();
            return doc;
        }

        public ResourceSet.Policy toDomain() {
            List<ResourceSet.Claim> claims = claimsRequired != null
                    ? claimsRequired.stream().map(ClaimDocument::toDomain).collect(Collectors.toList())
                    : new ArrayList<>();
            return new ResourceSet.Policy(name, scopes, claims);
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Set<String> getScopes() { return scopes; }
        public void setScopes(Set<String> scopes) { this.scopes = scopes; }
        public List<ClaimDocument> getClaimsRequired() { return claimsRequired; }
        public void setClaimsRequired(List<ClaimDocument> claimsRequired) { this.claimsRequired = claimsRequired; }
    }

    public static class ClaimDocument {
        private String name;
        private String friendlyName;
        private String claimType;
        private String value;
        private List<String> claimTokenFormat;
        private List<String> issuer;

        public ClaimDocument() {}

        public static ClaimDocument fromDomain(ResourceSet.Claim claim) {
            ClaimDocument doc = new ClaimDocument();
            doc.name = claim.getName();
            doc.friendlyName = claim.getFriendlyName();
            doc.claimType = claim.getClaimType();
            doc.value = claim.getValue();
            doc.claimTokenFormat = claim.getClaimTokenFormat();
            doc.issuer = claim.getIssuer();
            return doc;
        }

        public ResourceSet.Claim toDomain() {
            return new ResourceSet.Claim(name, friendlyName, claimType, value, claimTokenFormat, issuer);
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getFriendlyName() { return friendlyName; }
        public void setFriendlyName(String friendlyName) { this.friendlyName = friendlyName; }
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
