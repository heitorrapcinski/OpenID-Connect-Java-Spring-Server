package org.mitre.oidcprovider.application.service;

import org.mitre.oidcprovider.domain.exception.UserInfoNotFoundException;
import org.mitre.oidcprovider.domain.model.UserInfo;
import org.mitre.oidcprovider.domain.port.in.GetUserInfoUseCase;
import org.mitre.oidcprovider.domain.port.out.TokenIntrospectionPort;
import org.mitre.oidcprovider.domain.port.out.UserInfoRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserInfoService implements GetUserInfoUseCase {

    private final UserInfoRepository userInfoRepository;
    private final TokenIntrospectionPort tokenIntrospectionPort;

    public UserInfoService(UserInfoRepository userInfoRepository,
                           TokenIntrospectionPort tokenIntrospectionPort) {
        this.userInfoRepository = userInfoRepository;
        this.tokenIntrospectionPort = tokenIntrospectionPort;
    }

    @Override
    public UserInfo getUserInfo(String accessToken) {
        TokenIntrospectionPort.IntrospectionResult result = tokenIntrospectionPort.introspect(accessToken);

        if (!result.active()) {
            throw new UserInfoNotFoundException("Token is not active or invalid");
        }

        UserInfo userInfo = userInfoRepository.findBySub(result.sub())
                .orElseThrow(() -> new UserInfoNotFoundException("UserInfo not found for sub: " + result.sub()));

        Set<String> scopes = Arrays.stream(result.scope().split(" "))
                .collect(Collectors.toSet());

        return userInfo.filterByClaims(scopes);
    }
}
