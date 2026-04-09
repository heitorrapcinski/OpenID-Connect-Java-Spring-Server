package org.mitre.oidcprovider.domain.port.out;

public interface TokenIntrospectionPort {

    record IntrospectionResult(
            boolean active,
            String sub,
            String clientId,
            String scope,
            Long exp
    ) {}

    IntrospectionResult introspect(String accessToken);
}
