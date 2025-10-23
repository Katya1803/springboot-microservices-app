package com.example.common.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT Configuration Properties
 * Reads JWT settings from application.yml
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT secret key for signing tokens
     * Should be at least 256 bits (32 characters) for HS256
     */
    private String secret;

    /**
     * Access token expiration in milliseconds
     * Default: 15 minutes
     */
    private Long accessTokenExpiration = 900000L; // 15 minutes

    /**
     * Refresh token expiration in milliseconds
     * Default: 7 days
     */
    private Long refreshTokenExpiration = 604800000L; // 7 days

    /**
     * Service token expiration in milliseconds
     * Default: 5 minutes
     */
    private Long serviceTokenExpiration = 300000L; // 5 minutes

    /**
     * JWT issuer
     */
    private String issuer = "auth-service";
}