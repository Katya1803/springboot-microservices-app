package com.example.common.exception;

import com.example.common.constant.ErrorCode;
import com.example.common.dto.ApiResponse;
import com.example.common.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

/**
 * Global Exception Handler
 * Handles all exceptions across all microservices and returns standardized error responses
 *
 * CRITICAL: This prevents all exceptions from being converted to 403 by Spring Security
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle ResourceNotFoundException (404 Not Found)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            WebRequest request) {

        log.warn("Resource not found: {} - Path: {}",
                ex.getMessage(),
                request.getDescription(false));

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.RESOURCE_NOT_FOUND.getCode(),
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(errorResponse));
    }

    /**
     * Handle UnauthorizedException (401 Unauthorized)
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleUnauthorizedException(
            UnauthorizedException ex,
            WebRequest request) {

        log.warn("Unauthorized access: {} - Path: {}",
                ex.getMessage(),
                request.getDescription(false));

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.UNAUTHORIZED.getCode(),
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(errorResponse));
    }

    /**
     * Handle InvalidTokenException (401 Unauthorized)
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleInvalidTokenException(
            InvalidTokenException ex,
            WebRequest request) {

        log.warn("Invalid token: {} - Path: {}",
                ex.getMessage(),
                request.getDescription(false));

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_TOKEN.getCode(),
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(errorResponse));
    }

    /**
     * Handle AccessDeniedException (403 Forbidden)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest request) {

        log.warn("Access denied: {} - Path: {}",
                ex.getMessage(),
                request.getDescription(false));

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.ACCESS_DENIED.getCode(),
                "You don't have permission to access this resource"
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(errorResponse));
    }

    /**
     * Handle BadCredentialsException (401 Unauthorized)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleBadCredentialsException(
            BadCredentialsException ex,
            WebRequest request) {

        log.warn("Bad credentials: {} - Path: {}",
                ex.getMessage(),
                request.getDescription(false));

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_CREDENTIALS.getCode(),
                ErrorCode.INVALID_CREDENTIALS.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(errorResponse));
    }

    /**
     * Handle IllegalArgumentException (400 Bad Request)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {

        log.warn("Illegal argument: {} - Path: {}",
                ex.getMessage(),
                request.getDescription(false));

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_INPUT.getCode(),
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorResponse));
    }

    /**
     * Handle IllegalStateException (400 Bad Request)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleIllegalStateException(
            IllegalStateException ex,
            WebRequest request) {

        log.warn("Illegal state: {} - Path: {}",
                ex.getMessage(),
                request.getDescription(false));

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_INPUT.getCode(),
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorResponse));
    }

    /**
     * Handle MethodArgumentNotValidException (400 Bad Request)
     * Triggered by @Valid validation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        log.warn("Validation error: {} - Path: {}",
                ex.getMessage(),
                request.getDescription(false));

        var details = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    Object rejectedValue = ((FieldError) error).getRejectedValue();
                    return ErrorResponse.ErrorDetail.builder()
                            .field(fieldName)
                            .message(errorMessage)
                            .rejectedValue(rejectedValue)
                            .build();
                })
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.validation("Validation failed", details);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorResponse));
    }

    /**
     * Handle ConstraintViolationException (400 Bad Request)
     * Triggered by @Validated on class level
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleConstraintViolationException(
            ConstraintViolationException ex,
            WebRequest request) {

        log.warn("Constraint violation: {} - Path: {}",
                ex.getMessage(),
                request.getDescription(false));

        var details = ex.getConstraintViolations().stream()
                .map(violation -> ErrorResponse.ErrorDetail.builder()
                        .field(violation.getPropertyPath().toString())
                        .message(violation.getMessage())
                        .rejectedValue(violation.getInvalidValue())
                        .build())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.validation("Validation failed", details);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorResponse));
    }

    /**
     * Handle all other exceptions (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleGlobalException(
            Exception ex,
            WebRequest request) {

        log.error("Unexpected error: {} - Path: {}",
                ex.getMessage(),
                request.getDescription(false),
                ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                "An unexpected error occurred. Please try again later."
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(errorResponse));
    }
}