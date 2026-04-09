package org.mitre.scopemanager.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.mitre.scopemanager.domain.model.SystemScope;

public record ScopeResponse(
        @JsonProperty("value") String value,
        @JsonProperty("description") String description,
        @JsonProperty("icon") String icon,
        @JsonProperty("defaultScope") boolean defaultScope,
        @JsonProperty("restricted") boolean restricted
) {
    public static ScopeResponse fromDomain(SystemScope scope) {
        return new ScopeResponse(
                scope.getValue().value(),
                scope.getDescription(),
                scope.getIcon(),
                scope.isDefaultScope(),
                scope.isRestricted()
        );
    }
}
