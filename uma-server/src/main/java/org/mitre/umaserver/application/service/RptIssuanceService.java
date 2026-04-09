package org.mitre.umaserver.application.service;

import org.mitre.umaserver.domain.exception.InsufficientClaimsException;
import org.mitre.umaserver.domain.exception.ResourceSetNotFoundException;
import org.mitre.umaserver.domain.model.PermissionTicket;
import org.mitre.umaserver.domain.model.ResourceSet;
import org.mitre.umaserver.domain.model.vo.ClaimsSupplied;
import org.mitre.umaserver.domain.port.in.IssueRptUseCase;
import org.mitre.umaserver.domain.port.out.PermissionTicketRepository;
import org.mitre.umaserver.domain.port.out.ResourceSetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RptIssuanceService implements IssueRptUseCase {

    private final PermissionTicketRepository permissionTicketRepository;
    private final ResourceSetRepository resourceSetRepository;

    public RptIssuanceService(PermissionTicketRepository permissionTicketRepository,
                               ResourceSetRepository resourceSetRepository) {
        this.permissionTicketRepository = permissionTicketRepository;
        this.resourceSetRepository = resourceSetRepository;
    }

    @Override
    public String issueRpt(String ticketValue, List<ClaimsSupplied> claimsSupplied) {
        PermissionTicket ticket = permissionTicketRepository.findByTicketValue(ticketValue)
                .orElseThrow(() -> new ResourceSetNotFoundException("Permission ticket not found"));

        ticket.checkNotExpired();

        String resourceSetId = ticket.getPermission().resourceSetId();
        ResourceSet resourceSet = resourceSetRepository.findById(resourceSetId)
                .orElseThrow(() -> new ResourceSetNotFoundException(resourceSetId));

        List<ResourceSet.Policy> policies = resourceSet.getPolicies();
        if (policies != null && !policies.isEmpty()) {
            boolean satisfied = policies.stream().anyMatch(policy -> satisfiesPolicy(policy, claimsSupplied));
            if (!satisfied) {
                throw new InsufficientClaimsException();
            }
        }

        ticket.markUsed();
        permissionTicketRepository.save(ticket);

        return "RPT-" + UUID.randomUUID();
    }

    private boolean satisfiesPolicy(ResourceSet.Policy policy, List<ClaimsSupplied> claimsSupplied) {
        if (policy.getClaimsRequired() == null || policy.getClaimsRequired().isEmpty()) {
            return true;
        }
        return policy.getClaimsRequired().stream().allMatch(required ->
                claimsSupplied != null && claimsSupplied.stream().anyMatch(supplied ->
                        required.getName() != null &&
                        required.getName().equals(supplied.name()) &&
                        required.getValue() != null &&
                        required.getValue().equals(supplied.value())
                )
        );
    }
}
