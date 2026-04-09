package org.mitre.scopemanager.domain.model.vo;

/**
 * Value Object representing the unique identifier of a SystemScope.
 * Immutable by design (Java record). Validates that the value is not null or blank.
 */
public record ScopeValue(String value) {

    public ScopeValue {
        if (value == null) {
            throw new IllegalArgumentException("ScopeValue must not be null");
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException("ScopeValue must not be blank");
        }
    }
}
