package org.mitre.oidcprovider.domain.model;

import org.mitre.oidcprovider.domain.model.vo.PairwiseValue;
import org.mitre.oidcprovider.domain.model.vo.SectorIdentifier;
import org.mitre.oidcprovider.domain.model.vo.UserSub;

import java.util.UUID;

/**
 * Aggregate representing a pairwise pseudonymous identifier.
 * No framework annotations — pure domain object.
 */
public class PairwiseIdentifier {

    private final String id;
    private final UserSub userSub;
    private final SectorIdentifier sectorIdentifier;
    private final PairwiseValue pairwiseValue;

    private PairwiseIdentifier(String id, UserSub userSub, SectorIdentifier sectorIdentifier, PairwiseValue pairwiseValue) {
        this.id = id;
        this.userSub = userSub;
        this.sectorIdentifier = sectorIdentifier;
        this.pairwiseValue = pairwiseValue;
    }

    /** Factory method — creates a new PairwiseIdentifier with a generated UUID id. */
    public static PairwiseIdentifier create(UserSub userSub, SectorIdentifier sectorIdentifier, PairwiseValue pairwiseValue) {
        if (userSub == null) throw new IllegalArgumentException("UserSub must not be null");
        if (sectorIdentifier == null) throw new IllegalArgumentException("SectorIdentifier must not be null");
        if (pairwiseValue == null) throw new IllegalArgumentException("PairwiseValue must not be null");
        return new PairwiseIdentifier(UUID.randomUUID().toString(), userSub, sectorIdentifier, pairwiseValue);
    }

    /** Reconstitute from persistence. */
    public static PairwiseIdentifier reconstitute(String id, UserSub userSub, SectorIdentifier sectorIdentifier, PairwiseValue pairwiseValue) {
        return new PairwiseIdentifier(id, userSub, sectorIdentifier, pairwiseValue);
    }

    public String getId() { return id; }
    public UserSub getUserSub() { return userSub; }
    public SectorIdentifier getSectorIdentifier() { return sectorIdentifier; }
    public PairwiseValue getPairwiseValue() { return pairwiseValue; }
}
