package org.mitre.clientregistry.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.mitre.clientregistry.domain.model.Client;

import java.util.Set;
import java.util.stream.Collectors;

public record ClientResponse(
        @JsonProperty("client_id") String clientId,
        @JsonProperty("client_secret") String clientSecret,
        @JsonProperty("client_id_issued_at") long clientIdIssuedAt,
        @JsonProperty("client_secret_expires_at") long clientSecretExpiresAt,
        @JsonProperty("registration_access_token") String registrationAccessToken,
        @JsonProperty("registration_client_uri") String registrationClientUri,
        @JsonProperty("client_name") String clientName,
        @JsonProperty("redirect_uris") Set<String> redirectUris,
        @JsonProperty("grant_types") Set<String> grantTypes,
        @JsonProperty("response_types") Set<String> responseTypes,
        String scope,
        @JsonProperty("token_endpoint_auth_method") String tokenEndpointAuthMethod,
        @JsonProperty("subject_type") String subjectType
) {
    public static ClientResponse fromDomain(Client client, String serverBaseUrl) {
        String clientId = client.getClientId().value();

        Set<String> redirectUris = client.getRedirectUris() != null
                ? client.getRedirectUris().stream()
                        .map(ru -> ru.value())
                        .collect(Collectors.toSet())
                : null;

        Set<String> grantTypes = client.getGrantTypes() != null
                ? client.getGrantTypes().stream()
                        .map(gt -> gt.value())
                        .collect(Collectors.toSet())
                : null;

        Set<String> responseTypes = client.getResponseTypes() != null
                ? client.getResponseTypes().stream()
                        .map(rt -> rt.value())
                        .collect(Collectors.toSet())
                : null;

        String scope = client.getScope() != null
                ? String.join(" ", client.getScope())
                : null;

        long issuedAt = client.getCreatedAt() != null ? client.getCreatedAt().getEpochSecond() : 0L;

        return new ClientResponse(
                clientId,
                client.getClientSecret().value(),
                issuedAt,
                0L,
                client.getRegistrationAccessToken(),
                serverBaseUrl + "/register/" + clientId,
                client.getClientName(),
                redirectUris,
                grantTypes,
                responseTypes,
                scope,
                client.getTokenEndpointAuthMethod(),
                client.getSubjectType()
        );
    }
}
