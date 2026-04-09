package org.mitre.authserver.domain.model.vo;

import java.util.Collections;
import java.util.Set;

public record Scope(Set<String> values) {
    public Scope {
        if (values == null) {
            throw new IllegalArgumentException("Scope values must not be null");
        }
        values = Collections.unmodifiableSet(values);
    }

    public static Scope of(Set<String> values) {
        return new Scope(values);
    }

    public static Scope empty() {
        return new Scope(Collections.emptySet());
    }
}
