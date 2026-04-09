package org.mitre.umaserver.infrastructure.adapter.out.persistence;

import org.mitre.umaserver.domain.model.ResourceSet;
import org.mitre.umaserver.domain.port.out.ResourceSetRepository;
import org.mitre.umaserver.infrastructure.adapter.out.persistence.document.ResourceSetDocument;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MongoResourceSetRepository implements ResourceSetRepository {

    private final SpringDataResourceSetRepository springDataRepo;

    public MongoResourceSetRepository(SpringDataResourceSetRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public ResourceSet save(ResourceSet resourceSet) {
        ResourceSetDocument doc = ResourceSetDocument.fromDomain(resourceSet);
        ResourceSetDocument saved = springDataRepo.save(doc);
        return saved.toDomain();
    }

    @Override
    public Optional<ResourceSet> findById(String id) {
        return springDataRepo.findById(id).map(ResourceSetDocument::toDomain);
    }

    @Override
    public List<ResourceSet> findByOwner(String owner) {
        return springDataRepo.findByOwner(owner).stream()
                .map(ResourceSetDocument::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        springDataRepo.deleteById(id);
    }
}
