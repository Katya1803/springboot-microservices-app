package com.example.common.security.jwt;

import com.example.common.exception.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * JWT Token Validator
 * Validates and extracts information from JWT tokens
 * Used by Gateway and downstream services
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenValidator {

    private final JwtProperties jwtProperties;

    /**
     * Validate JWT token
     * @param token JWT token string
     * @return true if valid
     * @throws InvalidTokenException if token is invalid
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
            throw new InvalidTokenException("Invalid token signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
            throw new InvalidTokenException("Malformed token");
        } catch (ExpiredJwtException ex) {
            log.error("JWT token is expired: {}", ex.getMessage());
            throw new InvalidTokenException("Token expired");
        } catch (UnsupportedJwtException ex) {
            log.error("JWT token is unsupported: {}", ex.getMessage());
            throw new InvalidTokenException("Unsupported token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
            throw new InvalidTokenException("Token claims are empty");
        }
    }

    /**
     * Extract all claims from token
     */
    public Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Failed to extract claims from token: {}", e.getMessage());
            throw new InvalidTokenException("Failed to extract token claims");
        }
    }

    /**
     * Extract user ID from token
     */
    public String getUserId(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extract roles from token
     */
    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        Claims claims = getClaims(token);
        return claims.get("roles", List.class);
    }

    /**
     * Extract token type (USER_TOKEN or SERVICE_TOKEN)
     */
    public String getTokenType(String token) {
        return getClaims(token).get("token_type", String.class);
    }

    /**
     * Extract JTI (JWT ID) for blacklist checking
     */
    public String getJti(String token) {
        return getClaims(token).getId();
    }

    /**
     * Extract email from token
     */
    public String getEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    /**
     * Extract token expiration date
     */
    public Date getExpirationDate(String token) {
        return getClaims(token).getExpiration();
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDate(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Get remaining time until token expires (in seconds)
     */
    public long getRemainingTime(String token) {
        Date expiration = getExpirationDate(token);
        long now = System.currentTimeMillis();
        long expirationTime = expiration.getTime();
        return Math.max(0, (expirationTime - now) / 1000);
    }

    /**
     * Extract client ID (for service tokens)
     */
    public String getClientId(String token) {
        return getClaims(token).get("client_id", String.class);
    }

    /**
     * Extract scope (for service tokens)
     */
    public String getScope(String token) {
        return getClaims(token).get("scope", String.class);
    }

    /**
     * Extract audience (for service tokens)
     */
    public String getAudience(String token) {
        return getClaims(token).getAudience().iterator().next();
    }

    /**
     * Get signing key from secret
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}