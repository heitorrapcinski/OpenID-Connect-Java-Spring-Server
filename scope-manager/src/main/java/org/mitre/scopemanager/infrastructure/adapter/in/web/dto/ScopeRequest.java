package org.mitre.scopemanager.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ScopeRequest(
        @JsonProperty("value") String value,
        @JsonProperty("description") String description,
        @JsonProperty("icon") String icon,
        @JsonProperty("defaultScope") boolean defaultScope,
        @JsonProperty("restricted") boolean restricted
) {}
