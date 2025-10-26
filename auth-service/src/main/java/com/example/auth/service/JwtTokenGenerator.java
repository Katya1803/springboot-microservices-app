package com.example.auth.service;

import com.example.auth.entity.User;
import com.example.common.constant.SecurityConstants;
import com.example.common.security.jwt.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

/**
 * JWT Token Generator
 * Generates access tokens, refresh tokens, and service tokens
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenGenerator {

    private final JwtProperties jwtProperties;

    /**
     * Generate access token for user authentication
     */
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(jwtProperties.getAccessTokenExpiration());

        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", SecurityConstants.TOKEN_TYPE_USER);
        claims.put("roles", user.getRolesArray());
        claims.put("email", user.getEmail());
        claims.put("token_version", user.getTokenVersion());

        String token = Jwts.builder()
                .subject(user.getId())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .id(UUID.randomUUID().toString()) // JTI for blacklist
                .claims(claims)
                .signWith(getSigningKey())
                .compact();

        log.debug("Generated access token for user: {}, expires at: {}",
                user.getUsername(), expiration);

        return token;
    }

    /**
     * Generate service token for service-to-service communication
     */
    public String generateServiceToken(String clientId, String audience, String scope) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(jwtProperties.getServiceTokenExpiration());

        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", SecurityConstants.TOKEN_TYPE_SERVICE);
        claims.put("client_id", clientId);
        claims.put("scope", scope);
        claims.put("roles", List.of("ROLE_SERVICE")); // ← ADD THIS LINE!

        String token = Jwts.builder()
                .subject(clientId)
                .issuer(jwtProperties.getIssuer())
                .audience().add(audience).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .id(UUID.randomUUID().toString())
                .claims(claims)
                .signWith(getSigningKey())
                .compact();

        log.debug("Generated service token for client: {}, audience: {}, expires at: {}",
                clientId, audience, expiration);

        return token;
    }

    /**
     * Get signing key from secret
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Get access token expiration in seconds
     */
    public long getAccessTokenExpirationSeconds() {
        return jwtProperties.getAccessTokenExpiration() / 1000;
    }

    /**
     * Get service token expiration in seconds
     */
    public long getServiceTokenExpirationSeconds() {
        return jwtProperties.getServiceTokenExpiration() / 1000;
    }
}