package org.mitre.oidcprovider.infrastructure.adapter.out.persistence;

import org.mitre.oidcprovider.domain.model.ApprovedSite;
import org.mitre.oidcprovider.domain.port.out.ApprovedSiteRepository;
import org.mitre.oidcprovider.infrastructure.adapter.out.persistence.document.ApprovedSiteDocument;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MongoApprovedSiteRepository implements ApprovedSiteRepository {

    private final SpringDataApprovedSiteRepository springDataRepo;

    public MongoApprovedSiteRepository(SpringDataApprovedSiteRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public Optional<ApprovedSite> findByUserIdAndClientId(String userId, String clientId) {
        return springDataRepo.findByUserIdAndClientId(userId, clientId)
                .map(ApprovedSiteDocument::toDomain);
    }

    @Override
    public List<ApprovedSite> findByUserId(String userId) {
        return springDataRepo.findByUserId(userId).stream()
                .map(ApprovedSiteDocument::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public ApprovedSite save(ApprovedSite site) {
        return springDataRepo.save(ApprovedSiteDocument.fromDomain(site)).toDomain();
    }

    @Override
    public void deleteById(String id) {
        springDataRepo.deleteById(id);
    }
}
