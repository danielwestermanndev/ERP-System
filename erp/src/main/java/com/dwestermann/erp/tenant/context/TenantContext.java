package com.dwestermann.erp.tenant.context;

import org.springframework.stereotype.Component;

@Component
public class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static void setTenantId(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static String getTenantId() {
        return CURRENT_TENANT.get();
    }

    /**
     * Clear the tenant context for the current thread
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }

    /**
     * Check if tenant context is set
     */
    public static boolean hasTenantId() {
        return CURRENT_TENANT.get() != null;
    }
}