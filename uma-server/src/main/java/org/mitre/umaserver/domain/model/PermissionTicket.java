package org.mitre.umaserver.domain.model;

import org.mitre.umaserver.domain.exception.PermissionTicketExpiredException;
import org.mitre.umaserver.domain.model.vo.ClaimsSupplied;
import org.mitre.umaserver.domain.model.vo.Permission;
import org.mitre.umaserver.domain.model.vo.TicketValue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PermissionTicket {

    private String id;
    private final TicketValue ticket;
    private final Instant expiration;
    private final Permission permission;
    private List<ClaimsSupplied> claimsSupplied;
    private boolean used;

    private PermissionTicket(String id, TicketValue ticket, Instant expiration, Permission permission) {
        this.id = id;
        this.ticket = ticket;
        this.expiration = expiration;
        this.permission = permission;
        this.claimsSupplied = new ArrayList<>();
        this.used = false;
    }

    public static PermissionTicket create(TicketValue ticket, Instant expiration, Permission permission) {
        if (ticket == null) throw new IllegalArgumentException("PermissionTicket ticket must not be null");
        if (expiration == null) throw new IllegalArgumentException("PermissionTicket expiration must not be null");
        if (permission == null) throw new IllegalArgumentException("PermissionTicket permission must not be null");
        return new PermissionTicket(UUID.randomUUID().toString(), ticket, expiration, permission);
    }

    public void checkNotExpired() {
        if (Instant.now().isAfter(expiration)) {
            throw new PermissionTicketExpiredException();
        }
    }

    public void markUsed() {
        this.used = true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public TicketValue getTicket() { return ticket; }
    public Instant getExpiration() { return expiration; }
    public Permission getPermission() { return permission; }
    public List<ClaimsSupplied> getClaimsSupplied() { return claimsSupplied; }
    public void setClaimsSupplied(List<ClaimsSupplied> claimsSupplied) { this.claimsSupplied = claimsSupplied; }
    public boolean isUsed() { return used; }
}
