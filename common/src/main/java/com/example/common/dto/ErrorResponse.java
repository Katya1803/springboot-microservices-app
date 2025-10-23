package com.example.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Error Response Structure
 * Used for consistent error responses across all services
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * Error code (enum value)
     */
    private String code;

    /**
     * Human-readable error message
     */
    private String message;

    /**
     * Detailed error information (for validation errors)
     */
    private List<ErrorDetail> details;

    /**
     * Timestamp of error
     */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Request path where error occurred
     */
    private String path;

    /**
     * Trace ID for debugging
     */
    private String traceId;

    /**
     * Error Detail (for validation errors)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetail {
        /**
         * Field name that caused the error
         */
        private String field;

        /**
         * Error message for this field
         */
        private String message;

        /**
         * Rejected value
         */
        private Object rejectedValue;
    }

    // ========== Factory Methods ==========

    /**
     * Create simple error response
     */
    public static ErrorResponse of(String code, String message) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create error response with path
     */
    public static ErrorResponse of(String code, String message, String path) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .path(path)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create validation error response
     */
    public static ErrorResponse validation(String message, List<ErrorDetail> details) {
        return ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message(message)
                .details(details)
                .timestamp(Instant.now())
                .build();
    }
}