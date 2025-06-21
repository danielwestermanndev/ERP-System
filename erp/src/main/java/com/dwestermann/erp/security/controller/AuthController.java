package com.dwestermann.erp.security.controller;

import com.dwestermann.erp.security.dto.*;
import com.dwestermann.erp.security.service.AuthService;
import com.dwestermann.erp.tenant.context.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
// @CrossOrigin entfernt - wird über WebConfig gehandelt
public class AuthController {

    private final AuthService authService;

    /**
     * User Login - erstellt JWT Token mit Tenant-Kontext
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {} in tenant: {}",
                request.getEmail(), TenantContext.getTenantId());

        try {
            AuthResponse response = authService.authenticate(request);
            log.info("Successful login for user: {} in tenant: {}",
                    request.getEmail(), TenantContext.getTenantId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.warn("Failed login attempt for user: {} in tenant: {} - {}",
                    request.getEmail(), TenantContext.getTenantId(), e.getMessage());
            throw e;
        }
    }

    /**
     * User Registration - erstellt neuen User im aktuellen Tenant
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for user: {} in tenant: {}",
                request.getEmail(), TenantContext.getTenantId());

        AuthResponse response = authService.register(request);
        log.info("Successful registration for user: {} in tenant: {}",
                request.getEmail(), TenantContext.getTenantId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Refresh JWT Token
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Token refresh attempt in tenant: {}", TenantContext.getTenantId());

        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * User Logout - invalidiert Refresh Token
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        log.info("Logout for user in tenant: {}", TenantContext.getTenantId());

        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    /**
     * Validate Token - prüft ob aktueller JWT noch gültig ist
     */
    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken() {
        String tenantId = TenantContext.getTenantId();
        log.debug("Token validation in tenant: {}", tenantId);

        TokenValidationResponse response = TokenValidationResponse.builder()
                .valid(true)
                .tenantId(tenantId) // Aus TenantContext, nicht hardcoded
                .message("Token is valid")
                .validatedAt(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get current user info
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser() {
        String tenantId = TenantContext.getTenantId();
        log.debug("Get current user info in tenant: {}", tenantId);

        UserInfoResponse response = authService.getCurrentUserInfo();
        return ResponseEntity.ok(response);
    }

    /**
     * Change Password
     */
    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("Password change attempt in tenant: {}", TenantContext.getTenantId());

        authService.changePassword(request);

        MessageResponse response = MessageResponse.builder()
                .message("Password changed successfully")
                .success(true)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Request Password Reset
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Password reset request for email: {} in tenant: {}",
                request.getEmail(), TenantContext.getTenantId());

        authService.initiatePasswordReset(request.getEmail());

        MessageResponse response = MessageResponse.builder()
                .message("If the email exists, a password reset link has been sent")
                .success(true)
                .build();

        return ResponseEntity.ok(response);
    }
}