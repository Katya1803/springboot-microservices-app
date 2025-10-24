package com.example.gateway.service;

import com.example.gateway.security.JwtTokenValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Token Blacklist Service (Reactive)
 *
 * Uses Redis to check if a token has been blacklisted (revoked)
 * This is the reactive version for API Gateway
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String BLACKLIST_KEY_PREFIX = "blacklist:";

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final JwtTokenValidator jwtTokenValidator;

    /**
     * Check if token is blacklisted
     * @param token JWT token
     * @return Mono<Boolean> true if blacklisted
     */
    public Mono<Boolean> isBlacklisted(String token) {
        try {
            String jti = jwtTokenValidator.getJti(token);
            String key = BLACKLIST_KEY_PREFIX + jti;

            return reactiveRedisTemplate.hasKey(key)
                    .doOnSuccess(exists -> {
                        if (Boolean.TRUE.equals(exists)) {
                            log.debug("Token is blacklisted: {}", jti);
                        }
                    })
                    .onErrorResume(error -> {
                        log.error("Error checking blacklist: {}", error.getMessage());
                        // On Redis error, allow the request (fail open)
                        return Mono.just(false);
                    });

        } catch (Exception e) {
            log.error("Error extracting JTI from token: {}", e.getMessage());
            return Mono.just(false);
        }
    }
}