package org.mitre.oidcprovider.domain.port.in;

import org.mitre.oidcprovider.domain.model.UserInfo;

public interface GetUserInfoUseCase {
    UserInfo getUserInfo(String accessToken);
}
