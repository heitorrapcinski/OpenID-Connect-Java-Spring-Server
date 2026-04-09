package org.mitre.oidcprovider.domain.exception;

public class PairwiseIdentifierConflictException extends DomainException {
    public PairwiseIdentifierConflictException(String userSub, String sectorIdentifier) {
        super("Pairwise identifier conflict for userSub=" + userSub + ", sectorIdentifier=" + sectorIdentifier);
    }
}
