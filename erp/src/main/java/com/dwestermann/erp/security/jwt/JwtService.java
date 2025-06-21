package com.dwestermann.erp.security.jwt;

import com.dwestermann.erp.security.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
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
     * Extract role from JWT token
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extract user ID from JWT token
     */
    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    /**
     * Extract specific claim from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generate token for user with default expiration
     */
    public String generateToken(User user) {
        return generateToken(new HashMap<>(), user);
    }

    /**
     * Generate token for UserDetails with default expiration
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generate token with extra claims for User
     */
    public String generateToken(Map<String, Object> extraClaims, User user) {
        extraClaims.put("tenant", user.getTenantId());
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("userId", user.getId().toString());

        return buildToken(extraClaims, user.getEmail(), jwtExpiration);
    }

    /**
     * Generate token with extra claims for UserDetails
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails.getUsername(), jwtExpiration);
    }

    /**
     * Generate refresh token for user
     */
    public String generateRefreshToken(User user) {
        return generateRefreshToken(new HashMap<>(), user);
    }

    /**
     * Generate refresh token with extra claims
     */
    public String generateRefreshToken(Map<String, Object> extraClaims, User user) {
        extraClaims.put("tenant", user.getTenantId());
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("userId", user.getId().toString());
        extraClaims.put("type", "refresh");

        return buildToken(extraClaims, user.getEmail(), refreshExpiration);
    }

    /**
     * Get expiration time in seconds for access token
     */
    public long getExpirationTime() {
        return jwtExpiration / 1000; // Convert milliseconds to seconds
    }

    /**
     * Get expiration time in seconds for refresh token
     */
    public long getRefreshExpirationTime() {
        return refreshExpiration / 1000; // Convert milliseconds to seconds
    }

    /**
     * Validate token against user details
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Simple token validation (without user details)
     */
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
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
        return Jwts.parser()
                .verifyWith((SecretKey) getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Build JWT token
     */
    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Extract JWT token from Authorization header
     * Expected format: "Bearer <token>"
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7); // Remove "Bearer " prefix
    }

    /**
     * Check if Authorization header contains Bearer token
     */
    public boolean hasBearerToken(String authHeader) {
        return authHeader != null && authHeader.startsWith("Bearer ");
    }

    /**
     * Validate Authorization header format
     */
    public boolean isValidAuthorizationHeader(String authHeader) {
        return hasBearerToken(authHeader) && authHeader.length() > 7;
    }

    /**
     * Extract token from request header with validation
     */
    public String extractValidTokenFromHeader(String authHeader) {
        if (!isValidAuthorizationHeader(authHeader)) {
            throw new IllegalArgumentException("Invalid Authorization header format");
        }
        return extractTokenFromHeader(authHeader);
    }

    /**
     * Get signing key from secret
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}