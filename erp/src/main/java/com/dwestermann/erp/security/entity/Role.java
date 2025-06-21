package com.dwestermann.erp.security.entity;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Role Enum für hierarchisches Berechtigungssystem
 * Designed für Multi-Tenant SaaS mit verschiedenen Benutzerebenen
 */
@Getter
public enum Role {

    /**
     * Super Admin - Vollzugriff auf gesamte Plattform
     * Kann alle Tenants verwalten, System-Settings ändern
     */
    SUPER_ADMIN(Set.of(
            Permission.SYSTEM_ADMIN,
            Permission.TENANT_ADMIN,
            Permission.USER_MANAGEMENT,
            Permission.ACCOUNTING_FULL,
            Permission.CRM_FULL,
            Permission.INVENTORY_FULL,
            Permission.HR_FULL,
            Permission.PROJECT_MANAGEMENT_FULL,
            Permission.DOCUMENT_MANAGEMENT_FULL,
            Permission.REPORTING_FULL
    )),

    /**
     * Tenant Admin - Vollzugriff innerhalb eines Tenants
     * Kann alle Benutzer und Module im eigenen Tenant verwalten
     */
    TENANT_ADMIN(Set.of(
            Permission.TENANT_ADMIN,
            Permission.USER_MANAGEMENT,
            Permission.ACCOUNTING_FULL,
            Permission.CRM_FULL,
            Permission.INVENTORY_FULL,
            Permission.HR_FULL,
            Permission.PROJECT_MANAGEMENT_FULL,
            Permission.DOCUMENT_MANAGEMENT_FULL,
            Permission.REPORTING_FULL
    )),

    /**
     * Manager - Erweiterte Berechtigungen in Business-Modulen
     * Kann Berichte einsehen, Teams verwalten, aber keine User-Administration
     */
    MANAGER(Set.of(
            Permission.ACCOUNTING_WRITE,
            Permission.CRM_WRITE,
            Permission.INVENTORY_WRITE,
            Permission.HR_READ,
            Permission.PROJECT_MANAGEMENT_WRITE,
            Permission.DOCUMENT_MANAGEMENT_WRITE,
            Permission.REPORTING_READ
    )),

    /**
     * User - Standard Benutzer mit Basis-Berechtigungen
     * Kann eigene Daten bearbeiten und Basis-Funktionen nutzen
     */
    USER(Set.of(
            Permission.ACCOUNTING_READ,
            Permission.CRM_READ,
            Permission.INVENTORY_READ,
            Permission.PROJECT_MANAGEMENT_READ,
            Permission.DOCUMENT_MANAGEMENT_READ
    )),

    /**
     * Viewer - Nur Lesezugriff
     * Für externe Berater, Auditoren oder temporäre Zugriffe
     */
    VIEWER(Set.of(
            Permission.ACCOUNTING_READ,
            Permission.CRM_READ,
            Permission.INVENTORY_READ,
            Permission.REPORTING_READ
    ));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    /**
     * Konvertiert Role zu Spring Security Authorities
     * Fügt sowohl Rolle als auch individuelle Permissions hinzu
     */
    public List<SimpleGrantedAuthority> getAuthorities() {
        var authorities = getPermissions()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());

        // Füge Role als Authority hinzu (mit ROLE_ Prefix für Spring Security)
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));

        return authorities;
    }

    /**
     * Prüft ob diese Rolle eine bestimmte Permission hat
     */
    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    /**
     * Prüft ob diese Rolle eine andere Rolle "beinhaltet" (hierarchisch höher ist)
     */
    public boolean includes(Role otherRole) {
        return this.permissions.containsAll(otherRole.permissions);
    }

    /**
     * Gibt alle verfügbaren Rollen für einen Tenant zurück
     * (SUPER_ADMIN ist System-weit und nicht für normale Tenant-Zuordnung)
     */
    public static List<Role> getTenantRoles() {
        return Arrays.asList(TENANT_ADMIN, MANAGER, USER, VIEWER);
    }

    /**
     * Standard-Rolle für neue Registrierungen
     */
    public static Role getDefaultRole() {
        return USER;
    }
}