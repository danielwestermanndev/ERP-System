package com.dwestermann.erp.security.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Profile("dev") // Nur für Development
@Slf4j
public class DevPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        return hasPrivilege(authentication, targetDomainObject.toString(), permission.toString());
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return hasPrivilege(authentication, targetType, permission.toString());
    }

    private boolean hasPrivilege(Authentication authentication, String targetType, String permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();

        // Extract role from authorities
        String roleName = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5))
                .findFirst()
                .orElse("");

        log.debug("DevPermissionEvaluator: user={}, role={}, targetType={}, permission={}",
                username, roleName, targetType, permission);

        // For development: SUPER_ADMIN and TENANT_ADMIN have all permissions
        if ("SUPER_ADMIN".equals(roleName) || "TENANT_ADMIN".equals(roleName)) {
            log.debug("✅ Permission granted: {} is admin", roleName);
            return true;
        }

        // MANAGER has read/write permissions
        if ("MANAGER".equals(roleName) && ("read".equals(permission) || "write".equals(permission))) {
            log.debug("✅ Permission granted: MANAGER can read/write");
            return true;
        }

        // USER has read permissions
        if ("USER".equals(roleName) && "read".equals(permission)) {
            log.debug("✅ Permission granted: USER can read");
            return true;
        }

        log.debug("❌ Permission denied: role={}, permission={}", roleName, permission);
        return false;
    }
}