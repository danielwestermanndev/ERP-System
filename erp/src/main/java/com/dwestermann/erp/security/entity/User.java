package com.dwestermann.erp.security.entity;

import com.dwestermann.erp.common.entity.BaseEntity;
import com.dwestermann.erp.tenant.context.TenantContext;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"email", "tenant_id"})
        },
        indexes = {
                @Index(name = "idx_user_email_tenant", columnList = "email, tenant_id"),
                @Index(name = "idx_user_tenant", columnList = "tenant_id"),
                @Index(name = "idx_user_role", columnList = "role")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"password", "passwordResetToken", "emailVerificationToken"})
public class User extends BaseEntity implements UserDetails {

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    // Account Status Fields
    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "is_account_locked", nullable = false)
    @Builder.Default
    private boolean accountLocked = false;

    @Column(name = "is_password_change_required", nullable = false)
    @Builder.Default
    private boolean passwordChangeRequired = false;

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private int failedLoginAttempts = 0;

    // Timestamp Fields
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "account_locked_at")
    private LocalDateTime accountLockedAt;

    // Token Fields for Password Reset and Email Verification
    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_token_expires_at")
    private LocalDateTime passwordResetTokenExpiresAt;

    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    @Column(name = "email_verification_token_expires_at")
    private LocalDateTime emailVerificationTokenExpiresAt;

    // ========================================
    // UserDetails Implementation
    // ========================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Account expiration not implemented yet
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Password expiration not implemented yet
    }

    @Override
    public boolean isEnabled() {
        // Für Development: User ist enabled wenn nicht gesperrt
        // Für Production: Zusätzlich Email-Verification erforderlich
        return !accountLocked; // Email-Verification vorübergehend deaktiviert

        // Production Version (später aktivieren):
        // return emailVerified && !accountLocked;
    }

    // ========================================
    // Custom Getters for boolean fields
    // ========================================

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public boolean isPasswordChangeRequired() {
        return passwordChangeRequired;
    }

    // ========================================
    // Custom Setters für boolean fields
    // ========================================

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public void setPasswordChangeRequired(boolean passwordChangeRequired) {
        this.passwordChangeRequired = passwordChangeRequired;
    }

    // ========================================
    // Business Logic Methods
    // ========================================

    /**
     * Get full name for display purposes
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Get role names as Collection<String> for JWT claims
     * Since we have single role per user, returns Collection with one element
     */
    public Collection<String> getRoleNames() {
        return List.of(role.name());
    }

    /**
     * Get role name as String
     */
    public String getRoleName() {
        return role.name();
    }

    /**
     * Get all permission names for this user
     */
    public Collection<String> getPermissionNames() {
        return role.getPermissions()
                .stream()
                .map(permission -> permission.getPermission())
                .collect(Collectors.toList());
    }

    /**
     * Check if user has specific permission
     */
    public boolean hasPermission(Permission permission) {
        return role.hasPermission(permission);
    }

    /**
     * Check if user has specific permission by name
     */
    public boolean hasPermission(String permissionName) {
        return role.getPermissions()
                .stream()
                .anyMatch(p -> p.getPermission().equals(permissionName));
    }

    /**
     * Check if user has specific role
     */
    public boolean hasRole(Role checkRole) {
        return this.role.equals(checkRole);
    }

    /**
     * Check if user has role by name
     */
    public boolean hasRole(String roleName) {
        return this.role.name().equals(roleName);
    }

    /**
     * Check if password reset token is valid
     */
    public boolean isPasswordResetTokenValid() {
        return passwordResetToken != null &&
                passwordResetTokenExpiresAt != null &&
                passwordResetTokenExpiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * Check if email verification token is valid
     */
    public boolean isEmailVerificationTokenValid() {
        return emailVerificationToken != null &&
                emailVerificationTokenExpiresAt != null &&
                emailVerificationTokenExpiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * Clear password reset token
     */
    public void clearPasswordResetToken() {
        this.passwordResetToken = null;
        this.passwordResetTokenExpiresAt = null;
    }

    /**
     * Clear email verification token
     */
    public void clearEmailVerificationToken() {
        this.emailVerificationToken = null;
        this.emailVerificationTokenExpiresAt = null;
    }

    /**
     * Increment failed login attempts
     */
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
    }

    /**
     * Reset failed login attempts
     */
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
    }

    /**
     * Lock account
     */
    public void lockAccount() {
        this.accountLocked = true;
        this.accountLockedAt = LocalDateTime.now();
    }

    /**
     * Unlock account
     */
    public void unlockAccount() {
        this.accountLocked = false;
        this.accountLockedAt = null;
        this.failedLoginAttempts = 0;
    }

    // ========================================
    // JPA Lifecycle Callbacks
    // ========================================

    /**
     * Custom onCreate logic for User entity
     * Ruft parent onCreate auf und fügt User-spezifische Logik hinzu
     */
    @PrePersist
    @Override
    protected void onCreate() {
        // WICHTIG: Parent-Methode zuerst aufrufen für Tenant-Context und Audit
        super.onCreate();

        // User-spezifische onCreate-Logik
        if (this.passwordChangedAt == null) {
            this.passwordChangedAt = LocalDateTime.now();
        }

        // Email als lowercase für Konsistenz
        if (this.email != null) {
            this.email = this.email.toLowerCase().trim();
        }
    }

    /**
     * Custom onUpdate logic for User entity
     * Ruft parent onUpdate auf und fügt User-spezifische Logik hinzu
     */
    @PreUpdate
    @Override
    protected void onUpdate() {
        // WICHTIG: Parent-Methode zuerst aufrufen für Audit-Updates
        super.onUpdate();

        // User-spezifische onUpdate-Logik
        // Email als lowercase für Konsistenz
        if (this.email != null) {
            this.email = this.email.toLowerCase().trim();
        }
    }

    // ========================================
    // EQUALS & HASHCODE - Manual Implementation
    // ========================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        // Primary: Compare by ID if both exist
        if (this.getId() != null && user.getId() != null) {
            return this.getId().equals(user.getId());
        }

        // Secondary: Compare by email + tenantId for business equality
        return Objects.equals(email, user.email) &&
                Objects.equals(getTenantId(), user.getTenantId());
    }

    @Override
    public int hashCode() {
        // If ID exists, use it
        if (getId() != null) {
            return getId().hashCode();
        }

        // If no ID, use email + tenantId
        return Objects.hash(email, getTenantId());
    }
}