package org.mitre.umaserver.domain.port.out;

import org.mitre.umaserver.domain.model.ResourceSet;

import java.util.List;
import java.util.Optional;

public interface ResourceSetRepository {
    ResourceSet save(ResourceSet resourceSet);
    Optional<ResourceSet> findById(String id);
    List<ResourceSet> findByOwner(String owner);
    void deleteById(String id);
}
