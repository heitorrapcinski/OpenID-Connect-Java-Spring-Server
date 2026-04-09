package org.mitre.oidcprovider.domain.port.in;

import java.util.Map;

public interface GetJwksUseCase {
    Map<String, Object> getJwks();
}
