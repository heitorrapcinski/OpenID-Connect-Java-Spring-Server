package org.mitre.clientregistry.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public record RegisterClientRequest(
        @JsonProperty("client_name") String clientName,
        @JsonProperty("client_description") String clientDescription,
        @JsonProperty("redirect_uris") Set<String> redirectUris,
        @JsonProperty("grant_types") Set<String> grantTypes,
        @JsonProperty("response_types") Set<String> responseTypes,
        String scope,
        @JsonProperty("token_endpoint_auth_method") String tokenEndpointAuthMethod,
        @JsonProperty("application_type") String applicationType,
        @JsonProperty("subject_type") String subjectType,
        @JsonProperty("sector_identifier_uri") String sectorIdentifierUri,
        @JsonProperty("jwks_uri") String jwksUri,
        @JsonProperty("access_token_validity_seconds") Integer accessTokenValiditySeconds,
        @JsonProperty("refresh_token_validity_seconds") Integer refreshTokenValiditySeconds,
        @JsonProperty("id_token_validity_seconds") Integer idTokenValiditySeconds
) {}
