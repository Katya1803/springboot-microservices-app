package com.example.common.security.filter;

import com.example.common.constant.SecurityConstants;
import com.example.common.security.jwt.JwtTokenValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JWT Authentication Filter for Services
 * Supports TWO authentication modes:
 * 1. Gateway-injected headers (X-User-Id, X-User-Roles) - for user requests via Gateway
 * 2. JWT token in Authorization header - for service-to-service calls
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenValidator jwtTokenValidator;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // Mode 1: Check for Gateway-injected headers first (user requests)
            String userId = request.getHeader(SecurityConstants.HEADER_USER_ID);

            if (StringUtils.hasText(userId)) {
                // User request via Gateway - read from headers
                authenticateFromHeaders(request, userId);
            } else {
                // Mode 2: Service-to-service call - read from JWT token
                String token = extractToken(request);
                if (StringUtils.hasText(token)) {
                    authenticateFromToken(token, request);
                }
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Authenticate from Gateway-injected headers (user requests)
     */
    private void authenticateFromHeaders(HttpServletRequest request, String userId) {
        String rolesHeader = request.getHeader(SecurityConstants.HEADER_USER_ROLES);
        List<SimpleGrantedAuthority> authorities = parseRoles(rolesHeader);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("Authenticated from headers - User: {}, Roles: {}", userId, rolesHeader);
    }

    /**
     * Authenticate from JWT token (service-to-service calls)
     */
    private void authenticateFromToken(String token, HttpServletRequest request) {
        // Validate token
        jwtTokenValidator.validateToken(token);

        // Extract user info from token
        String userId = jwtTokenValidator.getUserId(token);
        List<String> roles = jwtTokenValidator.getRoles(token);

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("Authenticated from JWT - User: {}, Roles: {}", userId, roles);
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(SecurityConstants.JWT_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(SecurityConstants.JWT_PREFIX)) {
            return bearerToken.substring(SecurityConstants.JWT_PREFIX.length());
        }

        return null;
    }

    /**
     * Parse roles from comma-separated string
     */
    private List<SimpleGrantedAuthority> parseRoles(String rolesHeader) {
        if (!StringUtils.hasText(rolesHeader)) {
            return List.of();
        }

        return Stream.of(rolesHeader.split(","))
                .map(role -> new SimpleGrantedAuthority(role.trim()))
                .collect(Collectors.toList());
    }
}