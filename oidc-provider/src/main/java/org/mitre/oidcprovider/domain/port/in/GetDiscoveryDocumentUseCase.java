package org.mitre.oidcprovider.domain.port.in;

import java.util.Map;

public interface GetDiscoveryDocumentUseCase {
    Map<String, Object> getDiscoveryDocument(String issuerUrl);
}
