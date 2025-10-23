// auth-service/src/main/java/com/example/auth/service/AuthService.java
package com.example.auth.service;

import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LoginResponse;
import com.example.auth.dto.RefreshTokenRequest;
import com.example.auth.entity.User;
import com.example.auth.repository.jpa.UserRepository;
import com.example.common.constant.ErrorCode;
import com.example.common.exception.InvalidTokenException;
import com.example.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication Service
 * Handles user login, token refresh, and logout
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * User login
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        // Find user by username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS.getMessage()));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password for user: {}", request.getUsername());
            throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS.getMessage());
        }

        // Check if user is active
        if (!user.isActive()) {
            log.warn("User account is not active: {}", request.getUsername());
            throw new UnauthorizedException("User account is not active");
        }

        // Generate tokens
        String accessToken = jwtTokenGenerator.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user, request.getDeviceId());

        log.info("Login successful for user: {}", request.getUsername());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenGenerator.getAccessTokenExpirationSeconds())
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .roles(user.getRolesArray())
                        .build())
                .build();
    }

    /**
     * Refresh access token
     */
    @Transactional
    public LoginResponse refresh(RefreshTokenRequest request) {
        log.debug("Token refresh attempt");

        // Verify refresh token
        String userId = refreshTokenService.verifyRefreshToken(request.getRefreshToken());

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("User not found"));

        // Check if user is still active
        if (!user.isActive()) {
            refreshTokenService.revokeRefreshToken(request.getRefreshToken());
            throw new UnauthorizedException("User account is not active");
        }

        // Generate new tokens
        String newAccessToken = jwtTokenGenerator.generateAccessToken(user);

        // Rotate refresh token (revoke old, create new)
        refreshTokenService.revokeRefreshToken(request.getRefreshToken());
        String newRefreshToken = refreshTokenService.createRefreshToken(user, null);

        log.info("Token refresh successful for user: {}", user.getUsername());

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenGenerator.getAccessTokenExpirationSeconds())
                .build();
    }

    /**
     * Logout (revoke tokens)
     */
    @Transactional
    public void logout(String accessToken, String userId) {
        log.info("Logout for user: {}", userId);

        // Blacklist access token
        tokenBlacklistService.blacklistToken(accessToken);

        // Revoke all refresh tokens for user
        refreshTokenService.revokeAllUserTokens(userId);

        log.info("Logout successful for user: {}", userId);
    }
}