package com.dwestermann.erp.tenant.context;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TenantResolver {

    public String resolveTenantId(HttpServletRequest request) {
        // 1. Subdomain-basierte Erkennung (kunde1.yourerp.com)
        String tenantFromSubdomain = extractFromSubdomain(request);
        if (tenantFromSubdomain != null) {
            return tenantFromSubdomain;
        }

        // 2. Header-basierte Erkennung (für Development)
        String tenantFromHeader = request.getHeader("X-Tenant-ID");
        if (tenantFromHeader != null) {
            return tenantFromHeader;
        }

        // 3. Default für Development
        return "default";
    }

    private String extractFromSubdomain(HttpServletRequest request) {
        String serverName = request.getServerName();

        // Skip localhost und IP-Adressen
        if ("localhost".equals(serverName) || serverName.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
            return null;
        }

        // kunde1.yourerp.com -> kunde1
        String[] parts = serverName.split("\\.");
        if (parts.length >= 3) {
            return parts[0];
        }

        return null;
    }
}