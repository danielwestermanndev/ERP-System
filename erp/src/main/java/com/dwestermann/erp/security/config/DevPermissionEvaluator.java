package com.dwestermann.erp.security.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Development Permission Evaluator für @PreAuthorize Annotations
 * Implementiert permission-based authorization für das ERP-System
 */
@Component
@Profile("test")
@Slf4j
public class DevPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Extract permission string
        String permissionString = permission.toString();

        // Check if user has the required permission
        return hasPermission(authentication, permissionString);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Extract permission string
        String permissionString = permission.toString();

        // Build full permission: targetType:permission (e.g., "customer:read")
        String fullPermission = targetType + ":" + permissionString;

        return hasPermission(authentication, fullPermission);
    }

    /**
     * Prüft ob der User eine spezifische Permission hat
     */
    private boolean hasPermission(Authentication authentication, String permission) {
        // Get user authorities (roles and permissions)
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> {
                    String authorityName = authority.getAuthority();

                    // Check direct permission match
                    if (authorityName.equals(permission)) {
                        return true;
                    }

                    // Check role-based permissions
                    switch (authorityName) {
                        case "ROLE_SUPER_ADMIN":
                            return true; // Super Admin hat alle Permissions
                        case "ROLE_TENANT_ADMIN":
                            return hasAdminPermission(permission);
                        case "ROLE_MANAGER":
                            return hasManagerPermission(permission);
                        case "ROLE_USER":
                            return hasUserPermission(permission);
                        default:
                            return false;
                    }
                });
    }

    /**
     * Admin Permissions (fast alle außer Super Admin Features)
     */
    private boolean hasAdminPermission(String permission) {
        // Admin kann alles außer system-wide operations
        return !permission.startsWith("system:") && !permission.startsWith("tenant:");
    }

    /**
     * Manager Permissions (Read/Write für Business Objects)
     */
    private boolean hasManagerPermission(String permission) {
        if (permission.contains("delete") || permission.contains("admin")) {
            return false;
        }

        return permission.contains("customer:") ||
                permission.contains("product:") ||
                permission.contains("invoice:") ||
                permission.contains("order:");
    }

    /**
     * User Permissions (hauptsächlich Read)
     */
    private boolean hasUserPermission(String permission) {
        return permission.contains("read") ||
                permission.equals("customer:read") ||
                permission.equals("product:read") ||
                permission.equals("invoice:read");
    }
}