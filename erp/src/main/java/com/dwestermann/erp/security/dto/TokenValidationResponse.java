package com.dwestermann.erp.security.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TokenValidationResponse {

    private boolean valid;
    private String tenantId;
    private String message;
    private LocalDateTime validatedAt;
    private Long expiresIn; // seconds until expiration
}