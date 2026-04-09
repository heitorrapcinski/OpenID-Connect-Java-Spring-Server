package org.mitre.oidcprovider.infrastructure.adapter.out.persistence;

import org.mitre.oidcprovider.domain.model.UserInfo;
import org.mitre.oidcprovider.domain.port.out.UserInfoRepository;
import org.mitre.oidcprovider.infrastructure.adapter.out.persistence.document.UserInfoDocument;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MongoUserInfoRepository implements UserInfoRepository {

    private final SpringDataUserInfoRepository springDataRepo;

    public MongoUserInfoRepository(SpringDataUserInfoRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public Optional<UserInfo> findBySub(String sub) {
        return springDataRepo.findBySub(sub).map(UserInfoDocument::toDomain);
    }

    @Override
    public UserInfo save(UserInfo userInfo) {
        return springDataRepo.save(UserInfoDocument.fromDomain(userInfo)).toDomain();
    }

    @Override
    public Optional<UserInfo> findByPreferredUsername(String username) {
        return springDataRepo.findByPreferredUsername(username).map(UserInfoDocument::toDomain);
    }
}
