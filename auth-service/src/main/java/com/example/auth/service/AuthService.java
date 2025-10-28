package com.example.auth.service;

import com.example.auth.client.EmailServiceClient;
import com.example.auth.dto.*;
import com.example.auth.entity.User;
import com.example.auth.repository.jpa.UserRepository;
import com.example.common.constant.ErrorCode;
import com.example.common.dto.EmailRequest;
import com.example.common.exception.InvalidTokenException;
import com.example.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final OtpService otpService;
    private final EmailServiceClient emailServiceClient;
    private final UserEventPublisher userEventPublisher;

    /**
     * User registration with OTP
     * Status: PENDING until email verification
     */
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        log.info("Registration attempt for username: {}", request.getUsername());

        // Check duplicates
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create user with PENDING status
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles("ROLE_USER")
                .status("PENDING")  // ← User cannot login yet
                .emailVerified(false)
                .tokenVersion(1)
                .build();

        user = userRepository.save(user);

        // Generate OTP
        String otp = otpService.generateOtp(user.getEmail());

        // Send OTP email
        try {
            sendOtpEmail(user.getEmail(), user.getFirstName(), otp);
            log.info("OTP sent to email: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send OTP email: {}", e.getMessage(), e);
            // Continue registration even if email fails
        }

        log.info("User registered successfully with PENDING status: {}", user.getUsername());

        return RegisterResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .needsVerification(true)
                .message("Registration successful. Please check your email for OTP verification code.")
                .build();
    }

    /**
     * Verify OTP and activate account
     * Returns tokens for auto-login
     */
    @Transactional
    public LoginResponse verifyOtp(VerifyOtpRequest request) {
        log.info("OTP verification attempt for email: {}", request.getEmail());

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        // Check if already verified
        if (user.isActive()) {
            log.warn("User already verified: {}", request.getEmail());
            throw new IllegalStateException("Account already verified");
        }

        // Validate OTP
        boolean isValid = otpService.validateOtp(request.getEmail(), request.getOtp());
        if (!isValid) {
            log.warn("Invalid OTP for email: {}", request.getEmail());
            throw new UnauthorizedException("Invalid or expired OTP");
        }

        // Activate user
        user.activate();
        userRepository.save(user);
        log.info("User activated successfully: {}", user.getUsername());

        try {
            userEventPublisher.publishUserVerifiedEvent(user);
        } catch (Exception e) {
            log.error("Failed to publish user verification event: {}", e.getMessage(), e);
        }

        // Auto-login: Generate tokens
        String accessToken = jwtTokenGenerator.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(
                user,
                request.getDeviceId() != null ? request.getDeviceId() : "web"
        );

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
     * Resend OTP
     */
    @Transactional
    public void resendOtp(ResendOtpRequest request) {
        log.info("Resend OTP request for email: {}", request.getEmail());

        // Find user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        // Check if already verified
        if (user.isActive()) {
            throw new IllegalStateException("Account already verified");
        }

        // Check rate limit
        if (!otpService.canRequestOtp(request.getEmail())) {
            throw new IllegalStateException("Too many OTP requests. Please try again later.");
        }

        // Generate new OTP
        String otp = otpService.generateOtp(user.getEmail());
        otpService.incrementOtpRequest(user.getEmail());

        // Send OTP email
        try {
            sendOtpEmail(user.getEmail(), user.getFirstName(), otp);
            log.info("OTP resent to email: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to resend OTP email: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send OTP email");
        }
    }

    /**
     * User login
     * Only ACTIVE users can login
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

        // Check if pending verification
        if (user.isPending()) {
            log.warn("User account is pending verification: {}", request.getUsername());
            throw new UnauthorizedException("Please verify your email before logging in");
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
     * Token refresh
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
     * Logout
     */
    @Transactional
    public void logout(String accessToken, String userId) {
        log.info("Logout for user: {}", userId);

        tokenBlacklistService.blacklistToken(accessToken);
        refreshTokenService.revokeAllUserTokens(userId);

        log.info("Logout successful for user: {}", userId);
    }

    /**
     * Send OTP email
     */
    private void sendOtpEmail(String email, String firstName, String otp) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", firstName);
        variables.put("otp", otp);

        EmailRequest emailRequest = EmailRequest.builder()
                .to(email)
                .subject("Email Verification - Your OTP Code")
                .template("otp-email")
                .variables(variables)
                .build();

        emailServiceClient.sendEmail(emailRequest);
    }
}