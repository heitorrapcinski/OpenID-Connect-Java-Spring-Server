package org.mitre.oidcprovider.infrastructure.adapter.out.persistence.document;

import org.mitre.oidcprovider.domain.model.PairwiseIdentifier;
import org.mitre.oidcprovider.domain.model.vo.PairwiseValue;
import org.mitre.oidcprovider.domain.model.vo.SectorIdentifier;
import org.mitre.oidcprovider.domain.model.vo.UserSub;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("pairwise_identifiers")
@CompoundIndexes({
    @CompoundIndex(name = "idx_user_sub_sector", def = "{'userSub': 1, 'sectorIdentifier': 1}", unique = true)
})
public class PairwiseIdentifierDocument {

    @Id
    private String id;

    private String userSub;
    private String sectorIdentifier;
    private String identifier;

    public PairwiseIdentifierDocument() {}

    public static PairwiseIdentifierDocument fromDomain(PairwiseIdentifier identifier) {
        PairwiseIdentifierDocument doc = new PairwiseIdentifierDocument();
        doc.id = identifier.getId();
        doc.userSub = identifier.getUserSub().value();
        doc.sectorIdentifier = identifier.getSectorIdentifier().value();
        doc.identifier = identifier.getPairwiseValue().value();
        return doc;
    }

    public PairwiseIdentifier toDomain() {
        return PairwiseIdentifier.reconstitute(
                id,
                new UserSub(userSub),
                new SectorIdentifier(sectorIdentifier),
                new PairwiseValue(identifier)
        );
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserSub() { return userSub; }
    public void setUserSub(String userSub) { this.userSub = userSub; }
    public String getSectorIdentifier() { return sectorIdentifier; }
    public void setSectorIdentifier(String sectorIdentifier) { this.sectorIdentifier = sectorIdentifier; }
    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }
}
