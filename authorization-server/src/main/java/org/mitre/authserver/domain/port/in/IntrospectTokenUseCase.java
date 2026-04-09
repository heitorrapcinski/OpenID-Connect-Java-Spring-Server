package org.mitre.authserver.domain.port.in;

public interface IntrospectTokenUseCase {

    IntrospectionResult introspect(String token, String clientId);

    record IntrospectionResult(
            boolean active,
            String sub,
            String clientId,
            String scope,
            Long exp,
            Long iat,
            String tokenType
    ) {
        public static IntrospectionResult inactive() {
            return new IntrospectionResult(false, null, null, null, null, null, null);
        }
    }
}
