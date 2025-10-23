package com.example.common.util;

import com.example.common.constant.SecurityConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.UUID;

/**
 * Request Context Utility
 * Helper methods to access current request context
 */
@Slf4j
public final class RequestContextUtil {

    private RequestContextUtil() {
        // Prevent instantiation
    }

    /**
     * Get current HTTP request
     */
    public static Optional<HttpServletRequest> getCurrentRequest() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return Optional.ofNullable(attributes != null ? attributes.getRequest() : null);
        } catch (Exception e) {
            log.warn("Failed to get current request", e);
            return Optional.empty();
        }
    }

    /**
     * Get current user ID from SecurityContext
     */
    public static Optional<String> getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return Optional.of(authentication.getName());
            }
        } catch (Exception e) {
            log.warn("Failed to get current user ID", e);
        }
        return Optional.empty();
    }

    /**
     * Get user ID from request header (injected by Gateway)
     */
    public static Optional<String> getUserIdFromHeader() {
        return getCurrentRequest()
                .map(request -> request.getHeader(SecurityConstants.HEADER_USER_ID));
    }

    /**
     * Get user roles from request header (injected by Gateway)
     */
    public static Optional<String> getUserRolesFromHeader() {
        return getCurrentRequest()
                .map(request -> request.getHeader(SecurityConstants.HEADER_USER_ROLES));
    }

    /**
     * Get or generate request ID
     */
    public static String getRequestId() {
        return getCurrentRequest()
                .map(request -> {
                    String requestId = request.getHeader(SecurityConstants.HEADER_REQUEST_ID);
                    return requestId != null ? requestId : UUID.randomUUID().toString();
                })
                .orElseGet(() -> UUID.randomUUID().toString());
    }

    /**
     * Get request path
     */
    public static String getRequestPath() {
        return getCurrentRequest()
                .map(HttpServletRequest::getRequestURI)
                .orElse("unknown");
    }

    /**
     * Get client IP address
     */
    public static String getClientIpAddress() {
        return getCurrentRequest()
                .map(request -> {
                    String xForwardedFor = request.getHeader("X-Forwarded-For");
                    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                        return xForwardedFor.split(",")[0].trim();
                    }
                    return request.getRemoteAddr();
                })
                .orElse("unknown");
    }
}