package org.mitre.oidcprovider.infrastructure.adapter.out.persistence;

import org.mitre.oidcprovider.domain.model.PairwiseIdentifier;
import org.mitre.oidcprovider.domain.port.out.PairwiseIdentifierRepository;
import org.mitre.oidcprovider.infrastructure.adapter.out.persistence.document.PairwiseIdentifierDocument;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MongoPairwiseIdentifierRepository implements PairwiseIdentifierRepository {

    private final SpringDataPairwiseIdentifierRepository springDataRepo;

    public MongoPairwiseIdentifierRepository(SpringDataPairwiseIdentifierRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public Optional<PairwiseIdentifier> findByUserSubAndSectorIdentifier(String userSub, String sectorIdentifier) {
        return springDataRepo.findByUserSubAndSectorIdentifier(userSub, sectorIdentifier)
                .map(PairwiseIdentifierDocument::toDomain);
    }

    @Override
    public PairwiseIdentifier save(PairwiseIdentifier identifier) {
        return springDataRepo.save(PairwiseIdentifierDocument.fromDomain(identifier)).toDomain();
    }
}
