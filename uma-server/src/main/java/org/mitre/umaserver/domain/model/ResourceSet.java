package org.mitre.umaserver.domain.model;

import org.mitre.umaserver.domain.model.vo.ClientId;
import org.mitre.umaserver.domain.model.vo.Owner;
import org.mitre.umaserver.domain.model.vo.ResourceSetId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResourceSet {

    private final ResourceSetId id;
    private String name;
    private String uri;
    private String type;
    private Set<String> scopes;
    private String iconUri;
    private final Owner owner;
    private ClientId clientId;
    private List<Policy> policies;

    private ResourceSet(ResourceSetId id, String name, Owner owner, ClientId clientId) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.clientId = clientId;
        this.scopes = new HashSet<>();
        this.policies = new ArrayList<>();
    }

    public static ResourceSet create(ResourceSetId id, String name, Owner owner, ClientId clientId) {
        if (id == null) throw new IllegalArgumentException("ResourceSet id must not be null");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("ResourceSet name must not be null or blank");
        if (owner == null) throw new IllegalArgumentException("ResourceSet owner must not be null");
        return new ResourceSet(id, name, owner, clientId);
    }

    public void update(String name, String uri, String type, Set<String> scopes, String iconUri) {
        if (name != null && !name.isBlank()) this.name = name;
        this.uri = uri;
        this.type = type;
        this.scopes = scopes != null ? scopes : new HashSet<>();
        this.iconUri = iconUri;
    }

    public void addPolicy(Policy policy) {
        if (policy != null) {
            this.policies.add(policy);
        }
    }

    public void removePolicy(String policyName) {
        this.policies.removeIf(p -> p.getName().equals(policyName));
    }

    public ResourceSetId getId() { return id; }
    public String getName() { return name; }
    public String getUri() { return uri; }
    public String getType() { return type; }
    public Set<String> getScopes() { return scopes; }
    public String getIconUri() { return iconUri; }
    public Owner getOwner() { return owner; }
    public ClientId getClientId() { return clientId; }
    public List<Policy> getPolicies() { return policies; }
    public void setPolicies(List<Policy> policies) { this.policies = policies != null ? policies : new ArrayList<>(); }

    public static class Policy {
        private String name;
        private Set<String> scopes;
        private List<Claim> claimsRequired;

        public Policy() {
            this.scopes = new HashSet<>();
            this.claimsRequired = new ArrayList<>();
        }

        public Policy(String name, Set<String> scopes, List<Claim> claimsRequired) {
            this.name = name;
            this.scopes = scopes != null ? scopes : new HashSet<>();
            this.claimsRequired = claimsRequired != null ? claimsRequired : new ArrayList<>();
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Set<String> getScopes() { return scopes; }
        public void setScopes(Set<String> scopes) { this.scopes = scopes; }
        public List<Claim> getClaimsRequired() { return claimsRequired; }
        public void setClaimsRequired(List<Claim> claimsRequired) { this.claimsRequired = claimsRequired; }
    }

    public static class Claim {
        private String name;
        private String friendlyName;
        private String claimType;
        private String value;
        private List<String> claimTokenFormat;
        private List<String> issuer;

        public Claim() {}

        public Claim(String name, String friendlyName, String claimType, String value,
                     List<String> claimTokenFormat, List<String> issuer) {
            this.name = name;
            this.friendlyName = friendlyName;
            this.claimType = claimType;
            this.value = value;
            this.claimTokenFormat = claimTokenFormat;
            this.issuer = issuer;
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
