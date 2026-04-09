package org.mitre.oidcprovider.domain.port.in;

import org.mitre.oidcprovider.domain.model.PairwiseIdentifier;
import org.mitre.oidcprovider.domain.model.vo.SectorIdentifier;
import org.mitre.oidcprovider.domain.model.vo.UserSub;

public interface GeneratePairwiseIdentifierUseCase {
    PairwiseIdentifier generateOrGet(UserSub userSub, SectorIdentifier sectorIdentifier);
}
