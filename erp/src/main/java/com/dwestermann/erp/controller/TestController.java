package com.dwestermann.erp.controller;

import com.dwestermann.erp.tenant.context.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class TestController {

    /**
     * Public endpoint - no authentication required
     */
    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> publicEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is a public endpoint");
        response.put("timestamp", LocalDateTime.now());
        response.put("tenantId", TenantContext.getTenantId());
        return ResponseEntity.ok(response);
    }

    /**
     * Protected endpoint - authentication required
     */
    @GetMapping("/protected")
    public ResponseEntity<Map<String, Object>> protectedEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is a protected endpoint");
        response.put("timestamp", LocalDateTime.now());
        response.put("tenantId", TenantContext.getTenantId());
        response.put("authenticatedUser", auth.getName());
        response.put("authorities", auth.getAuthorities());

        return ResponseEntity.ok(response);
    }

    /**
     * Admin only endpoint
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> adminEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is an admin-only endpoint");
        response.put("timestamp", LocalDateTime.now());
        response.put("tenantId", TenantContext.getTenantId());
        response.put("authenticatedUser", auth.getName());
        response.put("authorities", auth.getAuthorities());

        return ResponseEntity.ok(response);
    }

    /**
     * Tenant info endpoint
     */
    @GetMapping("/tenant")
    public ResponseEntity<Map<String, Object>> tenantInfo() {
        String tenantId = TenantContext.getTenantId();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();
        response.put("tenantId", tenantId);
        response.put("timestamp", LocalDateTime.now());

        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            response.put("authenticatedUser", auth.getName());
            response.put("authorities", auth.getAuthorities());
        } else {
            response.put("authenticatedUser", null);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Echo endpoint - returns request information
     */
    @PostMapping("/echo")
    public ResponseEntity<Map<String, Object>> echo(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        response.put("received", payload);
        response.put("timestamp", LocalDateTime.now());
        response.put("tenantId", TenantContext.getTenantId());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            response.put("authenticatedUser", auth.getName());
        }

        return ResponseEntity.ok(response);
    }
}