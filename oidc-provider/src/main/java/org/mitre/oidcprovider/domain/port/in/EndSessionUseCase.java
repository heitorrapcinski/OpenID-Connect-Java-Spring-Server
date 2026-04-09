package org.mitre.oidcprovider.domain.port.in;

public interface EndSessionUseCase {
    void endSession(String idTokenHint, String postLogoutRedirectUri, String state);
}
