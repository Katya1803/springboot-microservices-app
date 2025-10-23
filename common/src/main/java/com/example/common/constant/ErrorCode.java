package com.example.common.constant;

/**
 * Error Code Enum
 * Centralized error codes for consistent error handling
 */
public enum ErrorCode {

    // ========== Authentication Errors (401) ==========
    INVALID_CREDENTIALS("Invalid username or password"),
    INVALID_TOKEN("Token is invalid or malformed"),
    TOKEN_EXPIRED("Token has expired"),
    TOKEN_REVOKED("Token has been revoked"),
    INVALID_REFRESH_TOKEN("Refresh token is invalid or expired"),
    UNAUTHORIZED("Authentication required"),

    // ========== Authorization Errors (403) ==========
    ACCESS_DENIED("You don't have permission to access this resource"),
    INSUFFICIENT_PERMISSIONS("Insufficient permissions"),
    FORBIDDEN("Access forbidden"),

    // ========== Resource Errors (404) ==========
    USER_NOT_FOUND("User not found"),
    RESOURCE_NOT_FOUND("Resource not found"),

    // ========== Validation Errors (400) ==========
    VALIDATION_ERROR("Validation failed"),
    INVALID_INPUT("Invalid input data"),
    MISSING_REQUIRED_FIELD("Required field is missing"),

    // ========== Rate Limiting (429) ==========
    RATE_LIMIT_EXCEEDED("Too many requests. Please try again later"),

    // ========== Service Errors (500) ==========
    INTERNAL_SERVER_ERROR("An unexpected error occurred"),
    SERVICE_UNAVAILABLE("Service temporarily unavailable"),

    // ========== OAuth2 Errors ==========
    INVALID_CLIENT("Invalid client credentials"),
    INVALID_GRANT("Invalid grant type"),
    INVALID_SCOPE("Invalid scope");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return this.name();
    }
}