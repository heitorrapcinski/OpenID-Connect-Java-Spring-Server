package org.mitre.oidcprovider.infrastructure.adapter.out.persistence;

import org.mitre.oidcprovider.domain.model.BlacklistedSite;
import org.mitre.oidcprovider.infrastructure.adapter.out.persistence.document.BlacklistedSiteDocument;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MongoBlacklistedSiteRepository {

    private final SpringDataBlacklistedSiteRepository springDataRepo;

    public MongoBlacklistedSiteRepository(SpringDataBlacklistedSiteRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    public Optional<BlacklistedSite> findByUri(String uri) {
        return springDataRepo.findByUri(uri).map(BlacklistedSiteDocument::toDomain);
    }

    public BlacklistedSite save(BlacklistedSite site) {
        return springDataRepo.save(BlacklistedSiteDocument.fromDomain(site)).toDomain();
    }

    public void deleteById(String id) {
        springDataRepo.deleteById(id);
    }

    public List<BlacklistedSite> findAll() {
        return springDataRepo.findAll().stream()
                .map(BlacklistedSiteDocument::toDomain)
                .collect(Collectors.toList());
    }
}
