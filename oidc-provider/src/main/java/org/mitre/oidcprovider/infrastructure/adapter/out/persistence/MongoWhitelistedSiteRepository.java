package org.mitre.oidcprovider.infrastructure.adapter.out.persistence;

import org.mitre.oidcprovider.domain.model.WhitelistedSite;
import org.mitre.oidcprovider.infrastructure.adapter.out.persistence.document.WhitelistedSiteDocument;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MongoWhitelistedSiteRepository {

    private final SpringDataWhitelistedSiteRepository springDataRepo;

    public MongoWhitelistedSiteRepository(SpringDataWhitelistedSiteRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    public Optional<WhitelistedSite> findByClientId(String clientId) {
        return springDataRepo.findByClientId(clientId).map(WhitelistedSiteDocument::toDomain);
    }

    public WhitelistedSite save(WhitelistedSite site) {
        return springDataRepo.save(WhitelistedSiteDocument.fromDomain(site)).toDomain();
    }

    public void deleteById(String id) {
        springDataRepo.deleteById(id);
    }

    public List<WhitelistedSite> findAll() {
        return springDataRepo.findAll().stream()
                .map(WhitelistedSiteDocument::toDomain)
                .collect(Collectors.toList());
    }
}
