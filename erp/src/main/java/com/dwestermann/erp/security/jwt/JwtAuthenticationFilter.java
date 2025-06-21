package com.dwestermann.erp.security.jwt;

import com.dwestermann.erp.security.service.CustomUserDetailsService;
import com.dwestermann.erp.tenant.context.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Skip authentication for certain paths
        if (shouldSkipAuthentication(request)) {
            log.debug("Skipping JWT authentication for path: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        log.debug("Processing request to: {} with Authorization header: {}",
                request.getRequestURI(),
                authHeader != null ? "Bearer ***" : "null");

        // Extract JWT token from Authorization header
        final String jwt = jwtService.extractTokenFromHeader(authHeader);
        if (jwt == null) {
            log.debug("No JWT token found in Authorization header");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract username and tenant from JWT
            final String userEmail = jwtService.extractUsername(jwt);
            final String tenantId = jwtService.extractTenantId(jwt);

            log.debug("Extracted from JWT - Email: {}, Tenant: {}", userEmail, tenantId);

            // Set tenant context from JWT
            if (tenantId != null) {
                TenantContext.setTenantId(tenantId);
                log.debug("Set tenant context to: {}", tenantId);
            }

            // If user is not already authenticated
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Validate token first
                if (!jwtService.isTokenValid(jwt)) {
                    log.warn("Invalid JWT token for user: {}", userEmail);
                    filterChain.doFilter(request, response);
                    return;
                }

                // Load user details
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                log.debug("Loaded user details for: {}", userEmail);

                // Double-check token validity with user details
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    // Set authentication details
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set authentication in SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.info("Successfully authenticated user: {} in tenant: {}", userEmail, tenantId);
                } else {
                    log.warn("JWT token validation failed for user: {}", userEmail);
                }
            }

        } catch (Exception e) {
            log.error("Error processing JWT token: {}", e.getMessage(), e);
            // Clear tenant context on error
            TenantContext.clear();
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Determine if authentication should be skipped for this request
     */
    private boolean shouldSkipAuthentication(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Skip authentication for public endpoints only
        return path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/register") ||
                path.startsWith("/api/auth/refresh") ||
                path.startsWith("/api/auth/forgot-password") ||
                path.startsWith("/api/test/public") ||
                path.startsWith("/api/test/tenant") ||
                path.startsWith("/h2-console/") ||
                path.startsWith("/actuator/") ||
                path.equals("/error") ||
                (path.startsWith("/api/test/") && "OPTIONS".equals(method)); // CORS preflight
    }
}