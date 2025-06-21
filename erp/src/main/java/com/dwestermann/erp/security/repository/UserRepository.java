package com.dwestermann.erp.security.repository;

import com.dwestermann.erp.security.entity.Role;
import com.dwestermann.erp.security.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository für User Entity mit Multi-Tenant Support
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // ========================================
    // Authentication & Core Queries
    // ========================================

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * Find user by email and tenant for authentication
     */
    Optional<User> findByEmailAndTenantId(String email, String tenantId);

    /**
     * Check if user exists by email and tenant
     */
    boolean existsByEmailAndTenantId(String email, String tenantId);

    /**
     * Find user by email in specific tenant (for login)
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.tenantId = :tenantId")
    Optional<User> findByEmailInTenant(@Param("email") String email, @Param("tenantId") String tenantId);

    // ========================================
    // Tenant-specific Queries
    // ========================================

    /**
     * Find all users in a tenant
     */
    List<User> findByTenantId(String tenantId);

    /**
     * Find all users in a tenant with pagination
     */
    Page<User> findByTenantId(String tenantId, Pageable pageable);

    /**
     * Count total users in tenant
     */
    long countByTenantId(String tenantId);

    /**
     * Count active users in tenant (not locked, email verified)
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenantId = :tenantId AND u.accountLocked = false AND u.emailVerified = true")
    long countActiveUsersByTenantId(@Param("tenantId") String tenantId);

    /**
     * Count users by role in tenant
     */
    long countByTenantIdAndRole(String tenantId, Role role);

    // ========================================
    // User Management Queries
    // ========================================

    /**
     * Find users by role in tenant
     */
    List<User> findByTenantIdAndRole(String tenantId, Role role);

    /**
     * Find users by role in tenant with pagination
     */
    Page<User> findByTenantIdAndRole(String tenantId, Role role, Pageable pageable);

    /**
     * Find locked users in tenant
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.accountLocked = true")
    List<User> findLockedUsersByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find users with unverified email in tenant
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.emailVerified = false")
    List<User> findUnverifiedUsersByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find users that require password change
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.passwordChangeRequired = true")
    List<User> findUsersRequiringPasswordChangeByTenantId(@Param("tenantId") String tenantId);

    // ========================================
    // Search & Filter Queries
    // ========================================

    /**
     * Search users by name or email in tenant
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND " +
            "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> searchUsersByTenantId(@Param("tenantId") String tenantId,
                                     @Param("searchTerm") String searchTerm,
                                     Pageable pageable);

    /**
     * Find users by first name in tenant
     */
    List<User> findByTenantIdAndFirstNameContainingIgnoreCase(String tenantId, String firstName);

    /**
     * Find users by last name in tenant
     */
    List<User> findByTenantIdAndLastNameContainingIgnoreCase(String tenantId, String lastName);

    // ========================================
    // Security & Audit Queries
    // ========================================

    /**
     * Find users with failed login attempts
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.failedLoginAttempts > 0")
    List<User> findUsersWithFailedLoginsByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find users with high failed login attempts (potential security risk)
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.failedLoginAttempts >= :threshold")
    List<User> findUsersWithHighFailedLoginsByTenantId(@Param("tenantId") String tenantId,
                                                       @Param("threshold") int threshold);

    /**
     * Find users who haven't logged in since a certain date
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND " +
            "(u.lastLoginAt IS NULL OR u.lastLoginAt < :since)")
    List<User> findInactiveUsersSince(@Param("tenantId") String tenantId,
                                      @Param("since") LocalDateTime since);

    // ========================================
    // Password Reset & Email Verification
    // ========================================

    /**
     * Find user by password reset token
     */
    @Query("SELECT u FROM User u WHERE u.passwordResetToken = :token AND " +
            "u.passwordResetTokenExpiresAt > :now")
    Optional<User> findByValidPasswordResetToken(@Param("token") String token,
                                                 @Param("now") LocalDateTime now);

    /**
     * Find user by email verification token
     */
    @Query("SELECT u FROM User u WHERE u.emailVerificationToken = :token AND " +
            "u.emailVerificationTokenExpiresAt > :now")
    Optional<User> findByValidEmailVerificationToken(@Param("token") String token,
                                                     @Param("now") LocalDateTime now);

    // ========================================
    // Admin Queries (Cross-Tenant für SUPER_ADMIN)
    // ========================================

    /**
     * Find all users across all tenants (SUPER_ADMIN only)
     */
    @Query("SELECT u FROM User u ORDER BY u.tenantId, u.email")
    Page<User> findAllUsersAcrossTenants(Pageable pageable);

    /**
     * Count users by role across all tenants
     */
    long countByRole(Role role);

    /**
     * Find users by role across all tenants
     */
    List<User> findByRole(Role role);

    // ========================================
    // Statistics Queries
    // ========================================

    /**
     * Get user registration statistics by tenant
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenantId = :tenantId AND " +
            "u.createdAt BETWEEN :startDate AND :endDate")
    long countNewUsersByTenantIdBetween(@Param("tenantId") String tenantId,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * Get login statistics by tenant
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenantId = :tenantId AND " +
            "u.lastLoginAt BETWEEN :startDate AND :endDate")
    long countActiveLoginsByTenantIdBetween(@Param("tenantId") String tenantId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    boolean existsByEmail(String email);  // Automatisch generiert

}