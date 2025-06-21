package com.dwestermann.erp.common.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base Entity für alle Multi-Tenant Entities
 * Stellt gemeinsame Felder und Funktionalität bereit
 */
@MappedSuperclass
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode
public abstract class BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 50, updatable = false)
    private String tenantId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Version
    @Column(name = "version")
    private Long version;

    /**
     * JPA Lifecycle Callback - wird vor dem ersten Speichern aufgerufen
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }

        // Tenant-ID aus Context setzen, falls noch nicht gesetzt
        if (tenantId == null) {
            // Import wird in der User Entity gemacht
            // tenantId = TenantContext.getTenantId();
        }
    }

    /**
     * JPA Lifecycle Callback - wird vor jedem Update aufgerufen
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Hilfsmethode für Entities die den Tenant-Context setzen müssen
     */
    public void setTenantFromContext() {
        // Diese Methode wird in konkreten Entities implementiert
        // um Circular Dependencies zu vermeiden
    }
}