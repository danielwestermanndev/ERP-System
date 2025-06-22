package com.dwestermann.erp.common.entity;

import com.dwestermann.erp.tenant.context.TenantContext;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder // WICHTIG: SuperBuilder für Vererbung
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "notes", length = 1000)
    private String notes;

    // ==================== JPA LIFECYCLE CALLBACKS ====================

    /**
     * Called before entity is persisted to database
     * Sets tenant context and audit information
     */
    @PrePersist
    protected void onCreate() {
        // Set tenant ID from context if not already set
        if (this.tenantId == null) {
            String contextTenantId = TenantContext.getTenantId();
            if (contextTenantId != null) {
                this.tenantId = contextTenantId;
            }
        }

        // Set audit fields if Spring Data JPA Auditing doesn't handle them
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        // Set created_by from security context if available
        if (this.createdBy == null) {
            this.createdBy = getCurrentUsername();
        }
    }

    /**
     * Called before entity is updated in database
     * Updates audit information
     */
    @PreUpdate
    protected void onUpdate() {
        // Update audit fields if Spring Data JPA Auditing doesn't handle them
        this.updatedAt = LocalDateTime.now();

        // Set updated_by from security context if available
        this.updatedBy = getCurrentUsername();
    }

    // ==================== TENANT VALIDATION ====================

    /**
     * Validates that the entity belongs to the current tenant
     * @throws IllegalStateException if tenant doesn't match
     */
    public void validateTenant() {
        String currentTenant = TenantContext.getTenantId();
        if (currentTenant != null && !currentTenant.equals(this.tenantId)) {
            throw new IllegalStateException(
                    String.format("Entity belongs to tenant '%s' but current tenant is '%s'",
                            this.tenantId, currentTenant)
            );
        }
    }

    /**
     * Checks if entity belongs to current tenant
     * @return true if tenant matches or no current tenant set
     */
    public boolean belongsToCurrentTenant() {
        String currentTenant = TenantContext.getTenantId();
        return currentTenant == null || currentTenant.equals(this.tenantId);
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Get current username from Spring Security context
     * @return username or "system" if no authentication context
     */
    private String getCurrentUsername() {
        try {
            var authentication = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();

                if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                    return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                } else {
                    return principal.toString();
                }
            }
        } catch (Exception e) {
            // Fall back to system if security context is not available
        }

        return "system";
    }

    /**
     * Check if entity is new (not yet persisted)
     * @return true if entity has no ID
     */
    public boolean isNew() {
        return this.id == null;
    }

    /**
     * Check if entity has been persisted
     * @return true if entity has an ID
     */
    public boolean isPersisted() {
        return this.id != null;
    }

    // ==================== EQUALS & HASHCODE ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity)) return false;

        BaseEntity that = (BaseEntity) o;

        // If both have IDs, compare by ID
        if (this.id != null && that.id != null) {
            return this.id.equals(that.id);
        }

        // If no IDs, fall back to default equals (reference equality)
        return false;
    }

    @Override
    public int hashCode() {
        // Use ID for hashCode if available, otherwise use class hashCode
        return id != null ? id.hashCode() : getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s{id=%s, tenantId='%s', version=%d}",
                getClass().getSimpleName(), id, tenantId, version);
    }
}