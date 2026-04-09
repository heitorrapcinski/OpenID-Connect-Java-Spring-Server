package org.mitre.oidcprovider.domain.port.out;

import org.mitre.oidcprovider.domain.model.ApprovedSite;

import java.util.List;
import java.util.Optional;

public interface ApprovedSiteRepository {
    Optional<ApprovedSite> findByUserIdAndClientId(String userId, String clientId);
    List<ApprovedSite> findByUserId(String userId);
    ApprovedSite save(ApprovedSite site);
    void deleteById(String id);
}
