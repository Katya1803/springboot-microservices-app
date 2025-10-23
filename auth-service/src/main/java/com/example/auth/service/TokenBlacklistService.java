package com.example.auth.service;

import com.example.common.constant.SecurityConstants;
import com.example.common.security.jwt.JwtTokenValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token Blacklist Service
 * Manages token revocation using Redis
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtTokenValidator jwtTokenValidator;

    /**
     * Add token to blacklist
     * TTL = remaining token lifetime
     */
    public void blacklistToken(String token) {
        try {
            // Extract JTI (JWT ID)
            String jti = jwtTokenValidator.getJti(token);

            // Calculate remaining time
            long remainingSeconds = jwtTokenValidator.getRemainingTime(token);

            if (remainingSeconds > 0) {
                // Add to blacklist with TTL
                String key = SecurityConstants.REDIS_BLACKLIST_PREFIX + jti;
                redisTemplate.opsForValue().set(key, "1", remainingSeconds, TimeUnit.SECONDS);

                log.info("Blacklisted token JTI: {}, TTL: {}s", jti, remainingSeconds);
            } else {
                log.debug("Token already expired, no need to blacklist: {}", jti);
            }

        } catch (Exception e) {
            log.error("Failed to blacklist token", e);
            throw new RuntimeException("Failed to blacklist token", e);
        }
    }

    /**
     * Check if token is blacklisted
     */
    public boolean isBlacklisted(String token) {
        try {
            String jti = jwtTokenValidator.getJti(token);
            String key = SecurityConstants.REDIS_BLACKLIST_PREFIX + jti;
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.warn("Failed to check blacklist status for token", e);
            return false;
        }
    }

    /**
     * Remove token from blacklist (for testing/admin purposes)
     */
    public void removeFromBlacklist(String jti) {
        String key = SecurityConstants.REDIS_BLACKLIST_PREFIX + jti;
        redisTemplate.delete(key);
        log.info("Removed JTI from blacklist: {}", jti);
    }
}