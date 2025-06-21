package com.dwestermann.erp.security.config;

import com.dwestermann.erp.security.entity.Permission;
import com.dwestermann.erp.security.entity.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Set;

@Component
@Slf4j
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return hasPrivilege(authentication, targetDomainObject.toString(), permission.toString());
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return hasPrivilege(authentication, targetType, permission.toString());
    }

    private boolean hasPrivilege(Authentication authentication, String targetType, String permission) {
        String username = authentication.getName();

        log.debug("Checking permission for user: {}", username);
        log.debug("User authorities: {}", authentication.getAuthorities());

        // Find ROLE_ authority (die Rolle ist immer mit ROLE_ prefixed)
        String roleName = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5)) // Remove "ROLE_" prefix
                .findFirst()
                .orElse("");

        log.debug("Extracted role: {} for user: {}", roleName, username);
        log.debug("Checking permission: targetType={}, permission={}", targetType, permission);

        if (roleName.isEmpty()) {
            log.warn("No role found for user: {}", username);
            return false;
        }

        try {
            Role role = Role.valueOf(roleName);
            Set<Permission> permissions = role.getPermissions();

            // Build expected permission string: CUSTOMER_READ, CUSTOMER_WRITE, etc.
            String requiredPermission = targetType.toUpperCase() + "_" + permission.toUpperCase();

            boolean hasPermission = permissions.stream()
                    .anyMatch(p -> p.name().equals(requiredPermission));

            log.debug("Permission check result: {} (required: {}, available: {})",
                    hasPermission, requiredPermission, permissions);

            return hasPermission;

        } catch (IllegalArgumentException e) {
            log.error("Invalid role: {} for user: {}", roleName, username);
            return false;
        }
    }
}