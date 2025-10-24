package com.example.auth.controller;

import com.example.auth.dto.*;
import com.example.auth.service.AuthService;
import com.example.common.constant.SecurityConstants;
import com.example.common.dto.ApiResponse;
import com.example.common.security.annotation.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * Handles user registration, login, OTP verification, token management
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /auth/register
     * Register new user (status: PENDING)
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("Registration request for username: {}", request.getUsername());

        RegisterResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, response.getMessage()));
    }

    /**
     * POST /auth/verify-otp
     * Verify OTP and activate account (auto-login)
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        log.info("OTP verification request for email: {}", request.getEmail());

        LoginResponse response = authService.verifyOtp(request);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Email verified successfully. You are now logged in.")
        );
    }

    /**
     * POST /auth/resend-otp
     * Resend OTP to email
     */
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(
            @Valid @RequestBody ResendOtpRequest request) {

        log.info("Resend OTP request for email: {}", request.getEmail());

        authService.resendOtp(request);

        return ResponseEntity.ok(
                ApiResponse.success("OTP sent successfully. Please check your email.")
        );
    }

    /**
     * POST /auth/login
     * User login (only ACTIVE users)
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
     * POST /auth/refresh
     * Refresh access token
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
     * POST /auth/logout
     * Logout and revoke tokens
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(SecurityConstants.JWT_HEADER) String authHeader,
            @CurrentUser String userId) {

        log.info("Logout request for user: {}", userId);

        String token = authHeader.replace(SecurityConstants.JWT_PREFIX, "");

        authService.logout(token, userId);

        return ResponseEntity.ok(
                ApiResponse.success("Logout successful")
        );
    }

    /**
     * GET /auth/health
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("Auth service is running")
        );
    }
}