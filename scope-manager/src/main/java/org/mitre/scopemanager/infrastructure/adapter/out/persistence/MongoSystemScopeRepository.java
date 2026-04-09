package org.mitre.scopemanager.infrastructure.adapter.out.persistence;

import org.mitre.scopemanager.domain.model.SystemScope;
import org.mitre.scopemanager.domain.port.out.SystemScopeRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MongoDB adapter implementing the domain output port SystemScopeRepository.
 * Translates between domain objects and MongoDB documents.
 */
@Component
public class MongoSystemScopeRepository implements SystemScopeRepository {

    private final SpringDataSystemScopeRepository springDataRepo;

    public MongoSystemScopeRepository(SpringDataSystemScopeRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public List<SystemScope> findAll() {
        return springDataRepo.findAll().stream()
                .map(SystemScopeDocument::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SystemScope> findByValue(String value) {
        return springDataRepo.findById(value).map(SystemScopeDocument::toDomain);
    }

    @Override
    public List<SystemScope> findAllDefault() {
        return springDataRepo.findByDefaultScopeTrue().stream()
                .map(SystemScopeDocument::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public SystemScope save(SystemScope scope) {
        SystemScopeDocument doc = SystemScopeDocument.fromDomain(scope);
        return springDataRepo.save(doc).toDomain();
    }

    @Override
    public void deleteByValue(String value) {
        springDataRepo.deleteById(value);
    }
}
