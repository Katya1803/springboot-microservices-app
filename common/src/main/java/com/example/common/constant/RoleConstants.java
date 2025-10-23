package com.example.common.constant;

/**
 * Role Constants
 * Centralized role definitions
 */
public final class RoleConstants {

    private RoleConstants() {
        // Prevent instantiation
    }

    /**
     * Admin role - full access
     */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    /**
     * User role - standard user access
     */
    public static final String ROLE_USER = "ROLE_USER";

    /**
     * Service role - for service-to-service communication
     */
    public static final String ROLE_SERVICE = "ROLE_SERVICE";

    // ========== Role Names (without ROLE_ prefix) ==========

    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";
    public static final String SERVICE = "SERVICE";
}