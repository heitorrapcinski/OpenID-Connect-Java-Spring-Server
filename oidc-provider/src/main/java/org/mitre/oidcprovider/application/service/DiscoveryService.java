package org.mitre.oidcprovider.application.service;

import org.mitre.oidcprovider.domain.port.in.GetDiscoveryDocumentUseCase;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DiscoveryService implements GetDiscoveryDocumentUseCase {

    @Override
    public Map<String, Object> getDiscoveryDocument(String issuerUrl) {
        LinkedHashMap<String, Object> doc = new LinkedHashMap<>();

        doc.put("issuer", issuerUrl);
        doc.put("authorization_endpoint", issuerUrl + "/authorize");
        doc.put("token_endpoint", issuerUrl + "/token");
        doc.put("userinfo_endpoint", issuerUrl + "/userinfo");
        doc.put("jwks_uri", issuerUrl + "/jwks");
        doc.put("registration_endpoint", issuerUrl + "/register");
        doc.put("end_session_endpoint", issuerUrl + "/end_session");
        doc.put("scopes_supported", List.of("openid", "profile", "email", "address", "phone", "offline_access"));
        doc.put("response_types_supported", List.of("code", "token", "id_token", "code token", "code id_token", "token id_token", "code token id_token"));
        doc.put("grant_types_supported", List.of("authorization_code", "implicit", "refresh_token", "client_credentials", "urn:ietf:params:oauth:grant-type:device_code"));
        doc.put("subject_types_supported", List.of("public", "pairwise"));
        doc.put("id_token_signing_alg_values_supported", List.of("RS256", "RS384", "RS512", "ES256", "ES384", "ES512", "PS256"));
        doc.put("token_endpoint_auth_methods_supported", List.of("client_secret_basic", "client_secret_post", "private_key_jwt", "none"));
        doc.put("claims_supported", List.of("sub", "name", "given_name", "family_name", "middle_name", "nickname", "preferred_username", "profile", "picture", "website", "email", "email_verified", "gender", "birthdate", "zoneinfo", "locale", "phone_number", "address", "updated_at"));
        doc.put("userinfo_signing_alg_values_supported", List.of("RS256", "RS384", "RS512", "ES256", "ES384", "ES512", "PS256", "none"));
        doc.put("request_parameter_supported", true);
        doc.put("request_uri_parameter_supported", false);
        doc.put("require_request_uri_registration", false);
        doc.put("claims_parameter_supported", false);

        return doc;
    }
}
