package org.mitre.authserver.infrastructure.adapter.out.persistence.document;

import org.mitre.authserver.domain.model.vo.AuthenticationHolder;

import java.util.Map;
import java.util.Set;

public class AuthenticationHolderDocument {

    private String clientId;
    private String userSub;
    private boolean approved;
    private String redirectUri;
    private Set<String> scope;
    private Set<String> responseTypes;
    private Map<String, String> requestParameters;
    private Set<String> authorities;

    public AuthenticationHolderDocument() {}

    public static AuthenticationHolderDocument fromDomain(AuthenticationHolder holder) {
        if (holder == null) return null;
        AuthenticationHolderDocument doc = new AuthenticationHolderDocument();
        doc.clientId = holder.clientId();
        doc.userSub = holder.userSub();
        doc.approved = holder.approved();
        doc.redirectUri = holder.redirectUri();
        doc.scope = holder.scope();
        doc.responseTypes = holder.responseTypes();
        doc.requestParameters = holder.requestParameters();
        doc.authorities = holder.authorities();
        return doc;
    }

    public AuthenticationHolder toDomain() {
        return new AuthenticationHolder(clientId, userSub, approved, redirectUri,
                scope, responseTypes, requestParameters, authorities);
    }

    // Getters and setters
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getUserSub() { return userSub; }
    public void setUserSub(String userSub) { this.userSub = userSub; }
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
    public String getRedirectUri() { return redirectUri; }
    public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
    public Set<String> getScope() { return scope; }
    public void setScope(Set<String> scope) { this.scope = scope; }
    public Set<String> getResponseTypes() { return responseTypes; }
    public void setResponseTypes(Set<String> responseTypes) { this.responseTypes = responseTypes; }
    public Map<String, String> getRequestParameters() { return requestParameters; }
    public void setRequestParameters(Map<String, String> requestParameters) { this.requestParameters = requestParameters; }
    public Set<String> getAuthorities() { return authorities; }
    public void setAuthorities(Set<String> authorities) { this.authorities = authorities; }
}
