package org.mitre.umaserver.infrastructure.adapter.in.web;

import org.mitre.umaserver.domain.model.PermissionTicket;
import org.mitre.umaserver.domain.model.vo.ClaimsSupplied;
import org.mitre.umaserver.domain.port.in.CreatePermissionTicketUseCase;
import org.mitre.umaserver.domain.port.in.IssueRptUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/uma/permission")
public class PermissionEndpoint {

    private final CreatePermissionTicketUseCase createTicketUseCase;
    private final IssueRptUseCase issueRptUseCase;

    public PermissionEndpoint(CreatePermissionTicketUseCase createTicketUseCase,
                               IssueRptUseCase issueRptUseCase) {
        this.createTicketUseCase = createTicketUseCase;
        this.issueRptUseCase = issueRptUseCase;
    }

    public record PermissionRequest(String resource_id, Set<String> resource_scopes) {}

    public record RptRequest(String ticket, List<ClaimsSupplied> claims_supplied) {}

    @PostMapping
    public ResponseEntity<Map<String, String>> createPermissionTicket(@RequestBody PermissionRequest request) {
        PermissionTicket ticket = createTicketUseCase.create(request.resource_id(), request.resource_scopes());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("ticket", ticket.getTicket().value()));
    }

    @PostMapping("/rpt")
    public ResponseEntity<Map<String, String>> issueRpt(@RequestBody RptRequest request) {
        String rpt = issueRptUseCase.issueRpt(request.ticket(), request.claims_supplied());
        return ResponseEntity.ok(Map.of("rpt", rpt));
    }
}
