package org.mitre.scopemanager.domain.model;

import org.mitre.scopemanager.domain.model.vo.ScopeValue;

/**
 * Aggregate Root for the scope-manager Bounded Context.
 * Represents a system-level OAuth 2.0 scope.
 * No framework annotations — pure domain object.
 */
public class SystemScope {

    private final ScopeValue value;
    private final String description;
    private final String icon;
    private final boolean defaultScope;
    private final boolean restricted;

    private SystemScope(ScopeValue value, String description, String icon,
                        boolean defaultScope, boolean restricted) {
        this.value = value;
        this.description = description;
        this.icon = icon;
        this.defaultScope = defaultScope;
        this.restricted = restricted;
    }

    /**
     * Factory method — the only way to create a SystemScope.
     */
    public static SystemScope create(ScopeValue value, String description, String icon,
                                     boolean defaultScope, boolean restricted) {
        return new SystemScope(value, description, icon, defaultScope, restricted);
    }

    /**
     * Returns a new SystemScope with updated fields (immutable update).
     */
    public SystemScope update(String description, String icon,
                              boolean defaultScope, boolean restricted) {
        return new SystemScope(this.value, description, icon, defaultScope, restricted);
    }

    public ScopeValue getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public boolean isDefaultScope() {
        return defaultScope;
    }

    public boolean isRestricted() {
        return restricted;
    }
}
