package org.mitre.oidcprovider.domain.port.out;

import org.mitre.oidcprovider.domain.model.PairwiseIdentifier;

import java.util.Optional;

public interface PairwiseIdentifierRepository {
    Optional<PairwiseIdentifier> findByUserSubAndSectorIdentifier(String userSub, String sectorIdentifier);
    PairwiseIdentifier save(PairwiseIdentifier identifier);
}
