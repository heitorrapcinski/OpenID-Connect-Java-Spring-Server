package org.mitre.oidcprovider.application.service;

import org.mitre.oidcprovider.domain.port.in.EndSessionUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EndSessionService implements EndSessionUseCase {

    private static final Logger log = LoggerFactory.getLogger(EndSessionService.class);

    @Override
    public void endSession(String idTokenHint, String postLogoutRedirectUri, String state) {
        log.info("End session request received: idTokenHint={}, postLogoutRedirectUri={}, state={}",
                idTokenHint, postLogoutRedirectUri, state);
        // No-op: session management is handled by the authorization server
    }
}
