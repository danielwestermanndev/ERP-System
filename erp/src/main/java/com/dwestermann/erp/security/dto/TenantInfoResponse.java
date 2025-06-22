package com.dwestermann.erp.security.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TenantInfoResponse {

    private String tenantId;
    private String tenantName;
    private String tenantDisplayName;
    private Boolean isActive;
    private String subscriptionType;
    private Integer maxUsers;
    private Integer currentUsers;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivity;
    private LocalDateTime retrievedAt;

    // Features/Settings
    private TenantSettings settings;

    @Data
    @Builder
    public static class TenantSettings {
        private Boolean multiCurrency;
        private String defaultCurrency;
        private String defaultLanguage;
        private String timeZone;
        private Boolean emailNotifications;
        private Boolean smsNotifications;
        private Boolean auditTrail;
        private Boolean documentManagement;
        private Boolean advancedReporting;
    }
}