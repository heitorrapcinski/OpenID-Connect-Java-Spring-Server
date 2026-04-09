package org.mitre.umaserver.domain.model.vo;

public record TicketValue(String value) {
    public TicketValue {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("TicketValue must not be null or blank");
        }
    }
}
