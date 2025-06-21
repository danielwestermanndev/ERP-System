package com.dwestermann.erp.security.entity;

import lombok.Getter;

/**
 * Permission Enum für granulare Berechtigungskontrolle
 * Folgt dem Prinzip: MODULE_ACTION (READ/WRITE/ADMIN)
 */
@Getter
public enum Permission {

    // System-Level Permissions
    SYSTEM_ADMIN("system:admin"),
    TENANT_ADMIN("tenant:admin"),
    USER_MANAGEMENT("user:management"),

    // Accounting Module
    ACCOUNTING_READ("accounting:read"),
    ACCOUNTING_WRITE("accounting:write"),
    ACCOUNTING_FULL("accounting:admin"),

    // Invoice specific permissions (Unterbereich von Accounting)
    INVOICE_CREATE("invoice:create"),
    INVOICE_READ("invoice:read"),
    INVOICE_UPDATE("invoice:update"),
    INVOICE_DELETE("invoice:delete"),
    INVOICE_SEND("invoice:send"),

    // Payment specific permissions
    PAYMENT_CREATE("payment:create"),
    PAYMENT_READ("payment:read"),
    PAYMENT_UPDATE("payment:update"),
    PAYMENT_DELETE("payment:delete"),

    // CRM Module
    CRM_READ("crm:read"),
    CRM_WRITE("crm:write"),
    CRM_FULL("crm:admin"),

    // Customer specific permissions
    CUSTOMER_CREATE("customer:create"),
    CUSTOMER_READ("customer:read"),
    CUSTOMER_UPDATE("customer:update"),
    CUSTOMER_DELETE("customer:delete"),

    // Inventory Module
    INVENTORY_READ("inventory:read"),
    INVENTORY_WRITE("inventory:write"),
    INVENTORY_FULL("inventory:admin"),

    // Product specific permissions
    PRODUCT_CREATE("product:create"),
    PRODUCT_READ("product:read"),
    PRODUCT_UPDATE("product:update"),
    PRODUCT_DELETE("product:delete"),

    // Stock management permissions
    STOCK_READ("stock:read"),
    STOCK_UPDATE("stock:update"),
    STOCK_TRANSFER("stock:transfer"),

    // HR Module
    HR_READ("hr:read"),
    HR_WRITE("hr:write"),
    HR_FULL("hr:admin"),

    // Employee specific permissions
    EMPLOYEE_CREATE("employee:create"),
    EMPLOYEE_READ("employee:read"),
    EMPLOYEE_UPDATE("employee:update"),
    EMPLOYEE_DELETE("employee:delete"),

    // Project Management Module
    PROJECT_MANAGEMENT_READ("project:read"),
    PROJECT_MANAGEMENT_WRITE("project:write"),
    PROJECT_MANAGEMENT_FULL("project:admin"),

    // Project specific permissions
    PROJECT_CREATE("project:create"),
    PROJECT_READ("project:read"),
    PROJECT_UPDATE("project:update"),
    PROJECT_DELETE("project:delete"),

    // Task specific permissions
    TASK_CREATE("task:create"),
    TASK_READ("task:read"),
    TASK_UPDATE("task:update"),
    TASK_DELETE("task:delete"),

    // Document Management Module
    DOCUMENT_MANAGEMENT_READ("document:read"),
    DOCUMENT_MANAGEMENT_WRITE("document:write"),
    DOCUMENT_MANAGEMENT_FULL("document:admin"),

    // Document specific permissions
    DOCUMENT_UPLOAD("document:upload"),
    DOCUMENT_READ("document:read"),
    DOCUMENT_UPDATE("document:update"),
    DOCUMENT_DELETE("document:delete"),
    DOCUMENT_SHARE("document:share"),

    // Reporting Module
    REPORTING_READ("reporting:read"),
    REPORTING_WRITE("reporting:write"),
    REPORTING_FULL("reporting:admin"),

    // Report specific permissions
    REPORT_VIEW("report:view"),
    REPORT_CREATE("report:create"),
    REPORT_EXPORT("report:export"),
    REPORT_SCHEDULE("report:schedule"),

    // Settings & Configuration
    SETTINGS_READ("settings:read"),
    SETTINGS_WRITE("settings:write"),

    // API Access
    API_ACCESS("api:access"),
    API_ADMIN("api:admin");

    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    /**
     * Extrahiert das Modul aus der Permission
     * z.B. "accounting:read" -> "accounting"
     */
    public String getModule() {
        return permission.split(":")[0];
    }

    /**
     * Extrahiert die Aktion aus der Permission
     * z.B. "accounting:read" -> "read"
     */
    public String getAction() {
        return permission.split(":")[1];
    }

    /**
     * Prüft ob Permission eine Lese-Berechtigung ist
     */
    public boolean isReadPermission() {
        return permission.endsWith(":read");
    }

    /**
     * Prüft ob Permission eine Schreib-Berechtigung ist
     */
    public boolean isWritePermission() {
        return permission.endsWith(":write") ||
                permission.endsWith(":create") ||
                permission.endsWith(":update") ||
                permission.endsWith(":delete");
    }

    /**
     * Prüft ob Permission eine Admin-Berechtigung ist
     */
    public boolean isAdminPermission() {
        return permission.endsWith(":admin") || permission.endsWith(":management");
    }
}