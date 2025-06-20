package com.dwestermann.erp.security.service;

import com.dwestermann.erp.security.dto.*;
import com.dwestermann.erp.security.entity.User;
import com.dwestermann.erp.security.entity.Role;
import com.dwestermann.erp.security.exception.AuthenticationException;
import com.dwestermann.erp.security.exception.UserAlreadyExistsException;
import com.dwestermann.erp.security.exception.InvalidPasswordException;
import com.dwestermann.erp.security.jwt.JwtService;
import com.dwestermann.erp.security.repository.UserRepository;
import com.dwestermann.erp.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Authenticate user and return JWT tokens
     */
    public AuthResponse authenticate(LoginRequest request) {
        String tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            throw new AuthenticationException("Tenant context is required for authentication");
        }

        try {
            // Authenticate with Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Load user from database (already tenant-aware through UserDetailsService)
            User user = userRepository.findByEmailAndTenantId(request.getEmail(), tenantId)
                    .orElseThrow(() -> new AuthenticationException("User not found"));

            // Check if account is locked
            if (user.isAccountLocked()) {
                throw new AuthenticationException("Account is locked");
            }

            // Update last login and reset failed attempts
            user.setLastLoginAt(LocalDateTime.now());
            user.setFailedLoginAttempts(0);
            userRepository.save(user);

            // Generate tokens
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            log.info("User {} authenticated successfully in tenant {}", user.getEmail(), tenantId);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getExpirationTime())
                    .userId(user.getId().toString())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .role(user.getRole())
                    .tenantId(tenantId)
                    .loginTime(LocalDateTime.now())
                    .isEmailVerified(user.isEmailVerified())
                    .requiresPasswordChange(user.isPasswordChangeRequired())
                    .build();

        } catch (BadCredentialsException e) {
            // Handle failed login attempt
            handleFailedLoginAttempt(request.getEmail(), tenantId);
            throw new AuthenticationException("Invalid email or password");
        }
    }

    /**
     * Register new user in current tenant
     */
    public AuthResponse register(RegisterRequest request) {
        String tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            throw new AuthenticationException("Tenant context is required for registration");
        }

        // Validate password confirmation
        if (!request.isPasswordConfirmed()) {
            throw new InvalidPasswordException("Password confirmation does not match");
        }

        // Check if user already exists
        if (userRepository.existsByEmailAndTenantId(request.getEmail(), tenantId)) {
            throw new UserAlreadyExistsException("User with this email already exists in this tenant");
        }

        // Create new user
        User user = User.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .isEmailVerified(false) // Require email verification
                .isAccountLocked(false)
                .isPasswordChangeRequired(false)
                .failedLoginAttempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        // Generate tokens for immediate login after registration
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("User {} registered successfully in tenant {}", user.getEmail(), tenantId);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .userId(user.getId().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .tenantId(tenantId)
                .loginTime(LocalDateTime.now())
                .isEmailVerified(user.isEmailVerified())
                .requiresPasswordChange(user.isPasswordChangeRequired())
                .build();
    }

    /**
     * Refresh JWT token
     */
    public AuthResponse refreshToken(String refreshToken) {
        String tenantId = TenantContext.getTenantId();

        if (!jwtService.isTokenValid(refreshToken)) {
            throw new AuthenticationException("Invalid refresh token");
        }

        String userEmail = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmailAndTenantId(userEmail, tenantId)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        if (user.isAccountLocked()) {
            throw new AuthenticationException("Account is locked");
        }

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .userId(user.getId().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .tenantId(tenantId)
                .loginTime(LocalDateTime.now())
                .isEmailVerified(user.isEmailVerified())
                .requiresPasswordChange(user.isPasswordChangeRequired())
                .build();
    }

    /**
     * Logout user by invalidating refresh token
     */
    public void logout(String refreshToken) {
        // In a real implementation, you would maintain a blacklist of tokens
        // or store refresh tokens in database/Redis for proper invalidation
        log.info("User logged out with refresh token ending in: {}",
                refreshToken.substring(Math.max(0, refreshToken.length() - 10)));
    }

    /**
     * Get current authenticated user information
     */
    public UserInfoResponse getCurrentUserInfo() {
        String tenantId = TenantContext.getTenantId();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("No authenticated user found");
        }

        String userEmail = authentication.getName();
        User user = userRepository.findByEmailAndTenantId(userEmail, tenantId)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        return UserInfoResponse.builder()
                .userId(user.getId().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .tenantId(user.getTenantId())
                .isEmailVerified(user.isEmailVerified())
                .isAccountLocked(user.isAccountLocked())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Change user password
     */
    public void changePassword(ChangePasswordRequest request) {
        String tenantId = TenantContext.getTenantId();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("No authenticated user found");
        }

        if (!request.isPasswordConfirmed()) {
            throw new InvalidPasswordException("Password confirmation does not match");
        }

        String userEmail = authentication.getName();
        User user = userRepository.findByEmailAndTenantId(userEmail, tenantId)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangeRequired(false);
        user.setPasswordChangedAt(LocalDateTime.now());

        userRepository.save(user);

        log.info("Password changed successfully for user {} in tenant {}", userEmail, tenantId);
    }

    /**
     * Initiate password reset process
     */
    public void initiatePasswordReset(String email) {
        String tenantId = TenantContext.getTenantId();

        // Find user (but don't reveal if user exists for security)
        userRepository.findByEmailAndTenantId(email, tenantId)
                .ifPresent(user -> {
                    // Generate password reset token
                    String resetToken = UUID.randomUUID().toString();
                    user.setPasswordResetToken(resetToken);
                    user.setPasswordResetTokenExpiresAt(LocalDateTime.now().plusHours(24));
                    userRepository.save(user);

                    // In real implementation, send email with reset link
                    log.info("Password reset initiated for user {} in tenant {}", email, tenantId);
                    // emailService.sendPasswordResetEmail(user, resetToken);
                });
    }

    /**
     * Handle failed login attempts
     */
    private void handleFailedLoginAttempt(String email, String tenantId) {
        userRepository.findByEmailAndTenantId(email, tenantId)
                .ifPresent(user -> {
                    int failedAttempts = user.getFailedLoginAttempts() + 1;
                    user.setFailedLoginAttempts(failedAttempts);

                    // Lock account after 5 failed attempts
                    if (failedAttempts >= 5) {
                        user.setAccountLocked(true);
                        user.setAccountLockedAt(LocalDateTime.now());
                        log.warn("Account locked for user {} in tenant {} due to {} failed login attempts",
                                email, tenantId, failedAttempts);
                    }

                    userRepository.save(user);
                });
    }
}