package com.example.common.constant;

/**
 * Security-related constants
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // Prevent instantiation
    }

    // ========== JWT Constants ==========

    /**
     * JWT header name
     */
    public static final String JWT_HEADER = "Authorization";

    /**
     * JWT token prefix
     */
    public static final String JWT_PREFIX = "Bearer ";

    /**
     * JWT issuer
     */
    public static final String JWT_ISSUER = "auth-service";

    // ========== Header Constants ==========

    /**
     * User ID header (injected by Gateway)
     */
    public static final String HEADER_USER_ID = "X-User-Id";

    /**
     * User roles header (injected by Gateway)
     */
    public static final String HEADER_USER_ROLES = "X-User-Roles";

    /**
     * Request ID header (for tracing)
     */
    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    // ========== Token Type Constants ==========

    /**
     * User access token
     */
    public static final String TOKEN_TYPE_USER = "USER_TOKEN";

    /**
     * Service-to-service token
     */
    public static final String TOKEN_TYPE_SERVICE = "SERVICE_TOKEN";

    // ========== Redis Key Prefixes ==========

    /**
     * Refresh token prefix
     */
    public static final String REDIS_REFRESH_TOKEN_PREFIX = "refresh_token:";

    /**
     * Blacklist token prefix
     */
    public static final String REDIS_BLACKLIST_PREFIX = "blacklist:";

    /**
     * Service token cache prefix
     */
    public static final String REDIS_SERVICE_TOKEN_PREFIX = "service_token:";

    /**
     * Rate limit prefix
     */
    public static final String REDIS_RATE_LIMIT_PREFIX = "rate_limit:";

    // ========== Token Expiration ==========

    /**
     * Access token expiration (15 minutes)
     */
    public static final long ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000L; // 15 minutes

    /**
     * Refresh token expiration (7 days)
     */
    public static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000L; // 7 days

    /**
     * Service token expiration (5 minutes)
     */
    public static final long SERVICE_TOKEN_EXPIRATION = 5 * 60 * 1000L; // 5 minutes
}