package com.example.auth.service;

import com.example.auth.entity.RefreshToken;
import com.example.auth.entity.User;
import com.example.auth.repository.redis.RefreshTokenRepository;
import com.example.common.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Refresh Token Service
 * Manages refresh tokens in Redis
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    // 7 days in seconds
    private static final long REFRESH_TOKEN_EXPIRATION_SECONDS = 7 * 24 * 60 * 60;

    /**
     * Create refresh token for user
     */
    public String createRefreshToken(User user, String deviceId) {
        // Generate random token
        String rawToken = UUID.randomUUID().toString() + ":" + user.getId();
        String tokenHash = hashToken(rawToken);

        // Create refresh token entity
        RefreshToken refreshToken = RefreshToken.builder()
                .id(tokenHash)
                .userId(user.getId())
                .deviceId(deviceId)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(REFRESH_TOKEN_EXPIRATION_SECONDS))
                .build();

        // Save to Redis
        refreshTokenRepository.save(refreshToken);

        log.info("Created refresh token for user: {}, device: {}",
                user.getUsername(), deviceId);

        // Return raw token (not hash)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(rawToken.getBytes());
    }

    /**
     * Verify refresh token and return user ID
     */
    public String verifyRefreshToken(String rawToken) {
        try {
            // Decode token
            String decodedToken = new String(Base64.getUrlDecoder().decode(rawToken));
            String tokenHash = hashToken(decodedToken);

            // Find in Redis
            RefreshToken refreshToken = refreshTokenRepository.findById(tokenHash)
                    .orElseThrow(() -> new InvalidTokenException("Refresh token not found or expired"));

            // Check expiration
            if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
                refreshTokenRepository.deleteById(tokenHash);
                throw new InvalidTokenException("Refresh token expired");
            }

            log.debug("Refresh token verified for user: {}", refreshToken.getUserId());
            return refreshToken.getUserId();

        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("Invalid refresh token format");
        }
    }

    /**
     * Revoke refresh token
     */
    public void revokeRefreshToken(String rawToken) {
        try {
            String decodedToken = new String(Base64.getUrlDecoder().decode(rawToken));
            String tokenHash = hashToken(decodedToken);
            refreshTokenRepository.deleteById(tokenHash);
            log.info("Revoked refresh token: {}", tokenHash);
        } catch (Exception e) {
            log.warn("Failed to revoke refresh token", e);
        }
    }

    /**
     * Revoke all refresh tokens for user
     */
    public void revokeAllUserTokens(String userId) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserId(userId);
        tokens.forEach(token -> refreshTokenRepository.deleteById(token.getId()));
        log.info("Revoked all refresh tokens for user: {}, count: {}", userId, tokens.size());
    }

    /**
     * Hash token using SHA-256
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}