package org.mitre.authserver.domain.model.vo;

import java.util.Map;
import java.util.Set;

public record AuthenticationHolder(
        String clientId,
        String userSub,
        boolean approved,
        String redirectUri,
        Set<String> scope,
        Set<String> responseTypes,
        Map<String, String> requestParameters,
        Set<String> authorities
) {}
