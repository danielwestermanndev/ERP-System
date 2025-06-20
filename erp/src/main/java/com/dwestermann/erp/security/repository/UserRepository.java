package com.dwestermann.erp.security.repository;

import com.dwestermann.erp.security.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email and tenant ID
     */
    Optional<User> findByEmailAndTenantId(String email, String tenantId);

    /**
     * Find all users for a specific tenant
     */
    List<User> findByTenantIdAndActiveTrue(String tenantId);

    /**
     * Find all users for a tenant with pagination
     */
    Page<User> findByTenantIdAndActiveTrue(String tenantId, Pageable pageable);

    /**
     * Check if email exists in tenant
     */
    boolean existsByEmailAndTenantId(String email, String tenantId);

    /**
     * Find user by email verification token
     */
    Optional<User> findByEmailVerificationToken(String token);

    /**
     * Find user by password reset token
     */
    Optional<User> findByPasswordResetToken(String token);

    /**
     * Find users by role in tenant
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.tenantId = :tenantId AND r = :role AND u.active = true")
    List<User> findByTenantIdAndRole(@Param("tenantId") String tenantId,
                                     @Param("role") User.UserRole role);

    /**
     * Count active users in tenant
     */
    long countByTenantIdAndActiveTrue(String tenantId);

    /**
     * Find users with failed login attempts
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.failedLoginAttempts >= :threshold")
    List<User> findUsersWithFailedAttempts(@Param("tenantId") String tenantId,
                                           @Param("threshold") int threshold);

    /**
     * Find locked users
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.lockedUntil > :now")
    List<User> findLockedUsers(@Param("tenantId") String tenantId,
                               @Param("now") LocalDateTime now);

    /**
     * Update last login timestamp
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    void updateLastLoginTime(@Param("userId") UUID userId,
                             @Param("loginTime") LocalDateTime loginTime);

    /**
     * Reset failed login attempts
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.lockedUntil = null WHERE u.id = :userId")
    void resetFailedLoginAttempts(@Param("userId") UUID userId);

    /**
     * Increment failed login attempts
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.id = :userId")
    void incrementFailedLoginAttempts(@Param("userId") UUID userId);

    /**
     * Lock user account
     */
    @Modifying
    @Query("UPDATE User u SET u.lockedUntil = :lockUntil WHERE u.id = :userId")
    void lockUserAccount(@Param("userId") UUID userId,
                         @Param("lockUntil") LocalDateTime lockUntil);

    /**
     * Verify email
     */
    @Modifying
    @Query("UPDATE User u SET u.emailVerified = true, u.emailVerificationToken = null WHERE u.id = :userId")
    void verifyEmail(@Param("userId") UUID userId);

    /**
     * Update password reset token
     */
    @Modifying
    @Query("UPDATE User u SET u.passwordResetToken = :token, u.passwordResetExpiresAt = :expiresAt WHERE u.id = :userId")
    void setPasswordResetToken(@Param("userId") UUID userId,
                               @Param("token") String token,
                               @Param("expiresAt") LocalDateTime expiresAt);

    /**
     * Clear password reset token
     */
    @Modifying
    @Query("UPDATE User u SET u.passwordResetToken = null, u.passwordResetExpiresAt = null WHERE u.id = :userId")
    void clearPasswordResetToken(@Param("userId") UUID userId);

    /**
     * Search users by name or email in tenant
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.active = true AND " +
            "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> searchUsers(@Param("tenantId") String tenantId,
                           @Param("searchTerm") String searchTerm,
                           Pageable pageable);
}