package org.mitre.umaserver.domain.model.vo;

import java.util.Set;

public record Permission(String resourceSetId, Set<String> scopes) {
    public Permission {
        if (resourceSetId == null) {
            throw new IllegalArgumentException("Permission resourceSetId must not be null");
        }
        if (scopes == null) {
            throw new IllegalArgumentException("Permission scopes must not be null");
        }
    }
}
