package org.mitre.oidcprovider.domain.port.out;

import org.mitre.oidcprovider.domain.model.UserInfo;

import java.util.Optional;

public interface UserInfoRepository {
    Optional<UserInfo> findBySub(String sub);
    UserInfo save(UserInfo userInfo);
    Optional<UserInfo> findByPreferredUsername(String username);
}
