package com.example.gateway.security;

/**
 * Security Constants for Gateway
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // Prevent instantiation
    }

    // JWT Header
    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_PREFIX = "Bearer ";

    // Token Types
    public static final String TOKEN_TYPE_USER = "USER_TOKEN";
    public static final String TOKEN_TYPE_SERVICE = "SERVICE_TOKEN";

    // Headers injected by Gateway for downstream services
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_ROLES = "X-User-Roles";
    public static final String HEADER_USER_EMAIL = "X-User-Email";
}