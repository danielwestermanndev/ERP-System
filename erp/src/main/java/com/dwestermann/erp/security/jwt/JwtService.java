package com.dwestermann.erp.security.jwt;

import com.dwestermann.erp.security.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    /**
     * Extract username from JWT token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract tenant ID from JWT token
     */
    public String extractTenantId(String token) {
        return extractClaim(token, claims -> claims.get("tenant", String.class));
    }

    /**
     * Extract user roles from JWT token
     */
    @SuppressWarnings("unchecked")
    public java.util.List<String> extractRoles(String token) {
        return extractClaim(token, claims -> claims.get("roles", java.util.List.class));
    }

    /**
     * Extract specific claim from JWT token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generate JWT token for user with tenant context
     */
    public String generateToken(UserDetails userDetails, String tenantId, java.util.List<String> roles) {
        return generateToken(new HashMap<>(), userDetails, tenantId, roles);
    }

    /**
     * Generate JWT token with extra claims
     */
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            String tenantId,
            java.util.List<String> roles
    ) {
        return buildToken(extraClaims, userDetails, tenantId, roles, jwtExpiration);
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshToken(UserDetails userDetails, String tenantId, java.util.List<String> roles) {
        return buildToken(new HashMap<>(), userDetails, tenantId, roles, refreshExpiration);
    }

    /**
     * Build JWT token with all claims
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            String tenantId,
            java.util.List<String> roles,
            long expiration
    ) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .claim("tenant", tenantId)
                .claim("roles", roles)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Validate token against user details
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if token is expired
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extract expiration date from token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.warn("JWT token is malformed: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.warn("JWT signature validation failed: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("JWT token compact of handler are invalid: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Get signing key for JWT
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extract token from Authorization header
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Validate token format and signature (without UserDetails)
     */
    public boolean isTokenValidFormat(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get token expiration time in milliseconds
     */
    public long getExpirationTime() {
        return jwtExpiration;
    }

    /**
     * Get refresh token expiration time in milliseconds
     */
    public long getRefreshExpirationTime() {
        return refreshExpiration;
    }

    /**
     * Generate refresh token with longer expiration
     */
    public String generateRefreshToken(User user) {
        return generateToken(new HashMap<>(), user, refreshExpiration);
    }

    /**
     * Generate token with custom expiration
     */
    private String generateToken(Map<String, Object> extraClaims, User user, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(user.getEmail())
                .claim("tenant", user.getTenantId())
                .claim("role", user.getRole().name())
                .claim("userId", user.getId().toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

}