package org.mitre.scopemanager.infrastructure.adapter.out.persistence;

import org.mitre.scopemanager.domain.model.SystemScope;
import org.mitre.scopemanager.domain.model.vo.ScopeValue;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * MongoDB document mapping for SystemScope aggregate root.
 * The _id field equals the scope value string (e.g., "openid").
 * Req 5.1: Aggregate Root persisted as MongoDB document with its identifier as _id.
 * Req 5.5: Unique index on value field.
 */
@Document("system_scopes")
public class SystemScopeDocument {

    @Id
    @Field("_id")
    private String id;

    @Indexed(unique = true)
    private String value;

    private String description;
    private String icon;
    private boolean defaultScope;
    private boolean restricted;

    /** No-args constructor required by Spring Data MongoDB. */
    public SystemScopeDocument() {
    }

    public SystemScopeDocument(String id, String value, String description, String icon,
                                boolean defaultScope, boolean restricted) {
        this.id = id;
        this.value = value;
        this.description = description;
        this.icon = icon;
        this.defaultScope = defaultScope;
        this.restricted = restricted;
    }

    /** Maps a domain SystemScope to a MongoDB document. */
    public static SystemScopeDocument fromDomain(SystemScope scope) {
        String val = scope.getValue().value();
        return new SystemScopeDocument(
                val,
                val,
                scope.getDescription(),
                scope.getIcon(),
                scope.isDefaultScope(),
                scope.isRestricted()
        );
    }

    /** Maps this document back to the domain SystemScope. */
    public SystemScope toDomain() {
        return SystemScope.create(
                new ScopeValue(value),
                description,
                icon,
                defaultScope,
                restricted
        );
    }

    public String getId() { return id; }
    public String getValue() { return value; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public boolean isDefaultScope() { return defaultScope; }
    public boolean isRestricted() { return restricted; }
}
