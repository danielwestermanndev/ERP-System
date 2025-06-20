package com.dwestermann.erp.security.jwt;

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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Skip JWT validation for certain paths
        if (shouldSkipJwtValidation(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt = jwtService.extractTokenFromHeader(authHeader);

        // No JWT token found
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract username and tenant from JWT
            final String userEmail = jwtService.extractUsername(jwt);
            final String tenantId = jwtService.extractTenantId(jwt);

            // Only proceed if user is not already authenticated
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Set tenant context first (important for UserDetailsService)
                if (tenantId != null) {
                    TenantContext.setTenantId(tenantId);
                    log.debug("Set tenant context from JWT: {}", tenantId);
                }

                // Load user details
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                // Validate token
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    // Set authentication details
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Successfully authenticated user: {} for tenant: {}", userEmail, tenantId);
                } else {
                    log.warn("Invalid JWT token for user: {}", userEmail);
                    // Clear tenant context on invalid token
                    TenantContext.clear();
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication from JWT: {}", e.getMessage());
            // Clear tenant context on any exception
            TenantContext.clear();
            // Clear security context
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Determine if JWT validation should be skipped for this request
     */
    private boolean shouldSkipJwtValidation(HttpServletRequest request) {
        String path = request.getServletPath();

        // Skip JWT for public endpoints
        return path.startsWith("/api/auth/") ||
                path.startsWith("/api/public/") ||
                path.startsWith("/api/test/") ||
                path.startsWith("/h2-console/") ||
                path.startsWith("/actuator/health") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/") ||
                path.equals("/favicon.ico");
    }
}