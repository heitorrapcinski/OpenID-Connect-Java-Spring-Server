package org.mitre.oidcprovider.infrastructure.adapter.in.web;

import org.mitre.oidcprovider.domain.model.UserInfo;
import org.mitre.oidcprovider.domain.model.vo.Address;
import org.mitre.oidcprovider.domain.port.in.GetUserInfoUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/userinfo")
public class UserInfoEndpoint {

    private final GetUserInfoUseCase getUserInfoUseCase;

    public UserInfoEndpoint(GetUserInfoUseCase getUserInfoUseCase) {
        this.getUserInfoUseCase = getUserInfoUseCase;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserInfoGet(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return handleUserInfo(authHeader);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> getUserInfoPost(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return handleUserInfo(authHeader);
    }

    private ResponseEntity<Map<String, Object>> handleUserInfo(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }
        String bearerToken = authHeader.substring("Bearer ".length());
        UserInfo userInfo = getUserInfoUseCase.getUserInfo(bearerToken);
        return ResponseEntity.ok(buildResponseMap(userInfo));
    }

    private Map<String, Object> buildResponseMap(UserInfo userInfo) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userInfo.getSub().value());

        if (userInfo.getPreferredUsername() != null) claims.put("preferred_username", userInfo.getPreferredUsername());
        if (userInfo.getName() != null) claims.put("name", userInfo.getName());
        if (userInfo.getGivenName() != null) claims.put("given_name", userInfo.getGivenName());
        if (userInfo.getFamilyName() != null) claims.put("family_name", userInfo.getFamilyName());
        if (userInfo.getMiddleName() != null) claims.put("middle_name", userInfo.getMiddleName());
        if (userInfo.getNickname() != null) claims.put("nickname", userInfo.getNickname());
        if (userInfo.getEmail() != null) claims.put("email", userInfo.getEmail());
        if (userInfo.getEmailVerified() != null) claims.put("email_verified", userInfo.getEmailVerified());
        if (userInfo.getPhoneNumber() != null) claims.put("phone_number", userInfo.getPhoneNumber());
        if (userInfo.getPhoneNumberVerified() != null) claims.put("phone_number_verified", userInfo.getPhoneNumberVerified());
        if (userInfo.getGender() != null) claims.put("gender", userInfo.getGender());
        if (userInfo.getBirthdate() != null) claims.put("birthdate", userInfo.getBirthdate());
        if (userInfo.getZoneinfo() != null) claims.put("zoneinfo", userInfo.getZoneinfo());
        if (userInfo.getLocale() != null) claims.put("locale", userInfo.getLocale());
        if (userInfo.getUpdatedTime() != null) claims.put("updated_at", userInfo.getUpdatedTime());
        if (userInfo.getProfile() != null) claims.put("profile", userInfo.getProfile());
        if (userInfo.getPicture() != null) claims.put("picture", userInfo.getPicture());
        if (userInfo.getWebsite() != null) claims.put("website", userInfo.getWebsite());

        Address address = userInfo.getAddress();
        if (address != null) {
            Map<String, Object> addressMap = new HashMap<>();
            if (address.formatted() != null) addressMap.put("formatted", address.formatted());
            if (address.streetAddress() != null) addressMap.put("street_address", address.streetAddress());
            if (address.locality() != null) addressMap.put("locality", address.locality());
            if (address.region() != null) addressMap.put("region", address.region());
            if (address.postalCode() != null) addressMap.put("postal_code", address.postalCode());
            if (address.country() != null) addressMap.put("country", address.country());
            claims.put("address", addressMap);
        }

        return claims;
    }
}
