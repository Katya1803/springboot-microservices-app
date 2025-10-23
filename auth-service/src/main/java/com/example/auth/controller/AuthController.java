package com.example.auth.controller;

import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LoginResponse;
import com.example.auth.dto.RefreshTokenRequest;
import com.example.auth.service.AuthService;
import com.example.common.constant.SecurityConstants;
import com.example.common.dto.ApiResponse;
import com.example.common.security.annotation.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * Handles user authentication endpoints
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * User login
     * POST /auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Login request for username: {}", request.getUsername());

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Login successful")
        );
    }

    /**
     * Refresh access token
     * POST /auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        log.debug("Token refresh request");

        LoginResponse response = authService.refresh(request);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Token refreshed successfully")
        );
    }

    /**
     * Logout (revoke tokens)
     * POST /auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(SecurityConstants.JWT_HEADER) String authHeader,
            @CurrentUser String userId) {

        log.info("Logout request for user: {}", userId);

        // Extract token from "Bearer <token>"
        String token = authHeader.replace(SecurityConstants.JWT_PREFIX, "");

        authService.logout(token, userId);

        return ResponseEntity.ok(
                ApiResponse.success("Logout successful")
        );
    }

    /**
     * Health check
     * GET /auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("Auth service is running")
        );
    }
}