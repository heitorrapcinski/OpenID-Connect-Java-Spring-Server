package org.mitre.umaserver.domain.port.in;

import org.mitre.umaserver.domain.model.ResourceSet;

import java.util.List;

public interface GetResourceSetUseCase {
    ResourceSet getById(String id);
    List<ResourceSet> getByOwner(String owner);
}
