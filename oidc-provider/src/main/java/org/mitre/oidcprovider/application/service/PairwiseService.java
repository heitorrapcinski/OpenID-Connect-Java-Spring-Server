package org.mitre.oidcprovider.application.service;

import org.mitre.oidcprovider.domain.model.PairwiseIdentifier;
import org.mitre.oidcprovider.domain.model.vo.PairwiseValue;
import org.mitre.oidcprovider.domain.model.vo.SectorIdentifier;
import org.mitre.oidcprovider.domain.model.vo.UserSub;
import org.mitre.oidcprovider.domain.port.in.GeneratePairwiseIdentifierUseCase;
import org.mitre.oidcprovider.domain.port.out.PairwiseIdentifierRepository;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class PairwiseService implements GeneratePairwiseIdentifierUseCase {

    private final PairwiseIdentifierRepository pairwiseIdentifierRepository;

    public PairwiseService(PairwiseIdentifierRepository pairwiseIdentifierRepository) {
        this.pairwiseIdentifierRepository = pairwiseIdentifierRepository;
    }

    @Override
    public PairwiseIdentifier generateOrGet(UserSub userSub, SectorIdentifier sectorIdentifier) {
        Optional<PairwiseIdentifier> existing = pairwiseIdentifierRepository
                .findByUserSubAndSectorIdentifier(userSub.value(), sectorIdentifier.value());

        if (existing.isPresent()) {
            return existing.get();
        }

        String input = userSub.value() + "|" + sectorIdentifier.value();
        String hex = sha256Hex(input);
        PairwiseValue pairwiseValue = new PairwiseValue(hex);
        PairwiseIdentifier identifier = PairwiseIdentifier.create(userSub, sectorIdentifier, pairwiseValue);
        return pairwiseIdentifierRepository.save(identifier);
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
