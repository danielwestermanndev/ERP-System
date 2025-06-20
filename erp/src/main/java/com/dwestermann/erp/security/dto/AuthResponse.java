// AuthResponse.java
package com.dwestermann.erp.security.dto;

import com.dwestermann.erp.security.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn; // seconds until expiration

    // User information
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private String tenantId;

    // Additional metadata
    private LocalDateTime loginTime;
    private boolean isEmailVerified;
    private boolean requiresPasswordChange;
}