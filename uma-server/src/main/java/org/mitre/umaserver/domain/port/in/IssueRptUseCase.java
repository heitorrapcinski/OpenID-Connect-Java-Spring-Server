package org.mitre.umaserver.domain.port.in;

import org.mitre.umaserver.domain.model.vo.ClaimsSupplied;

import java.util.List;

public interface IssueRptUseCase {
    String issueRpt(String ticketValue, List<ClaimsSupplied> claimsSupplied);
}
