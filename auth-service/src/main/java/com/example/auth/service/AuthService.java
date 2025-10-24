package com.example.auth.service;

import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LoginResponse;
import com.example.auth.dto.RefreshTokenRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.dto.RegisterResponse;
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
     * User registration
     */
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        log.info("Registration attempt for username: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles("ROLE_USER")
                .status("ACTIVE")
                .tokenVersion(1)
                .build();

        user = userRepository.save(user);

        log.info("User registered successfully: {}", user.getUsername());

        return RegisterResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .message("Registration successful")
                .build();
    }

    /**
     * User login
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS.getMessage()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password for user: {}", request.getUsername());
            throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS.getMessage());
        }

        if (!user.isActive()) {
            log.warn("User account is not active: {}", request.getUsername());
            throw new UnauthorizedException("User account is not active");
        }

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

        String userId = refreshTokenService.verifyRefreshToken(request.getRefreshToken());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("User not found"));

        if (!user.isActive()) {
            refreshTokenService.revokeRefreshToken(request.getRefreshToken());
            throw new UnauthorizedException("User account is not active");
        }

        String newAccessToken = jwtTokenGenerator.generateAccessToken(user);

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

        tokenBlacklistService.blacklistToken(accessToken);
        refreshTokenService.revokeAllUserTokens(userId);

        log.info("Logout successful for user: {}", userId);
    }
}