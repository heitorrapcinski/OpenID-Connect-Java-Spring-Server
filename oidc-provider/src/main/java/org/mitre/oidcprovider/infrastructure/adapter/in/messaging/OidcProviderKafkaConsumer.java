package org.mitre.oidcprovider.infrastructure.adapter.in.messaging;

import org.mitre.oidcprovider.domain.port.in.GeneratePairwiseIdentifierUseCase;
import org.mitre.oidcprovider.domain.port.out.PairwiseIdentifierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OidcProviderKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(OidcProviderKafkaConsumer.class);

    private final GeneratePairwiseIdentifierUseCase generatePairwiseIdentifierUseCase;
    private final PairwiseIdentifierRepository pairwiseIdentifierRepository;

    public OidcProviderKafkaConsumer(GeneratePairwiseIdentifierUseCase generatePairwiseIdentifierUseCase,
                                     PairwiseIdentifierRepository pairwiseIdentifierRepository) {
        this.generatePairwiseIdentifierUseCase = generatePairwiseIdentifierUseCase;
        this.pairwiseIdentifierRepository = pairwiseIdentifierRepository;
    }

    @KafkaListener(topics = "client.registered", groupId = "oidc-provider-client-events-infra")
    @SuppressWarnings("unchecked")
    public void onClientRegistered(@Payload Map<String, Object> event) {
        Map<String, Object> payload = (Map<String, Object>) event.get("payload");
        if (payload != null) {
            String subjectType = (String) payload.get("subjectType");
            String sectorIdentifierUri = (String) payload.get("sectorIdentifierUri");

            if ("pairwise".equals(subjectType) && sectorIdentifierUri != null) {
                log.info("Pairwise setup needed for client: clientId={}, sectorIdentifierUri={}",
                        payload.get("clientId"), sectorIdentifierUri);
            }
        }
        log.info("Received client.registered event: {}", event);
    }

    @KafkaListener(topics = "auth.token.issued", groupId = "oidc-provider-audit-infra")
    public void onTokenIssued(@Payload Map<String, Object> event) {
        log.info("Token issued event received for audit: {}", event);
    }
}
