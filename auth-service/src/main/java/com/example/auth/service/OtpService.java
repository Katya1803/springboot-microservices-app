package com.example.auth.service;

import com.example.auth.entity.OtpCode;
import com.example.auth.repository.jpa.OtpCodeRepository;
import com.example.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * OTP Service
 * Handles OTP generation, validation, and cleanup
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpCodeRepository otpCodeRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String OTP_REDIS_PREFIX = "otp:";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${otp.expiration:300000}") // 5 minutes default
    private long otpExpiration;

    @Value("${otp.length:6}")
    private int otpLength;

    /**
     * Generate OTP code
     */
    @Transactional
    public String generateOtp(String email) {
        log.info("Generating OTP for email: {}", email);

        // Generate random OTP
        String code = generateRandomCode();

        // Calculate expiration
        Instant expiresAt = Instant.now().plusMillis(otpExpiration);

        // Save to database
        OtpCode otpCode = OtpCode.builder()
                .email(email)
                .code(code)
                .expiresAt(expiresAt)
                .build();

        otpCodeRepository.save(otpCode);

        // Also store in Redis for faster lookup
        String redisKey = OTP_REDIS_PREFIX + email;
        redisTemplate.opsForValue().set(
                redisKey,
                code,
                Duration.ofMillis(otpExpiration)
        );

        log.info("OTP generated successfully for email: {}", email);
        return code;
    }

    /**
     * Validate OTP code
     */
    @Transactional
    public boolean validateOtp(String email, String code) {
        log.info("Validating OTP for email: {}", email);

        // Check Redis first (faster)
        String redisKey = OTP_REDIS_PREFIX + email;
        String cachedOtp = redisTemplate.opsForValue().get(redisKey);

        if (cachedOtp != null && cachedOtp.equals(code)) {
            // Mark as verified in database
            Optional<OtpCode> otpOpt = otpCodeRepository.findByEmailAndCodeAndVerifiedFalse(email, code);
            if (otpOpt.isPresent()) {
                OtpCode otp = otpOpt.get();
                if (otp.isValid()) {
                    otp.setVerified(true);
                    otpCodeRepository.save(otp);

                    // Remove from Redis
                    redisTemplate.delete(redisKey);

                    log.info("OTP validated successfully for email: {}", email);
                    return true;
                }
            }
        }

        // Fallback to database
        Optional<OtpCode> otpOpt = otpCodeRepository.findByEmailAndCodeAndVerifiedFalse(email, code);
        if (otpOpt.isPresent()) {
            OtpCode otp = otpOpt.get();
            if (otp.isValid()) {
                otp.setVerified(true);
                otpCodeRepository.save(otp);

                // Remove from Redis
                redisTemplate.delete(redisKey);

                log.info("OTP validated successfully from database for email: {}", email);
                return true;
            } else {
                log.warn("OTP expired for email: {}", email);
                throw new UnauthorizedException("OTP has expired");
            }
        }

        log.warn("Invalid OTP for email: {}", email);
        return false;
    }

    /**
     * Check if user can request new OTP (rate limiting)
     */
    public boolean canRequestOtp(String email) {
        String rateLimitKey = "otp:ratelimit:" + email;
        String attempts = redisTemplate.opsForValue().get(rateLimitKey);

        if (attempts != null) {
            int count = Integer.parseInt(attempts);
            if (count >= 3) { // Max 3 requests per 15 minutes
                log.warn("OTP rate limit exceeded for email: {}", email);
                return false;
            }
        }

        return true;
    }

    /**
     * Increment OTP request counter
     */
    public void incrementOtpRequest(String email) {
        String rateLimitKey = "otp:ratelimit:" + email;
        Long count = redisTemplate.opsForValue().increment(rateLimitKey);

        if (count != null && count == 1) {
            // Set expiration on first request
            redisTemplate.expire(rateLimitKey, 15, TimeUnit.MINUTES);
        }
    }

    /**
     * Generate random numeric code
     */
    private String generateRandomCode() {
        int min = (int) Math.pow(10, otpLength - 1);
        int max = (int) Math.pow(10, otpLength) - 1;
        int code = RANDOM.nextInt(max - min + 1) + min;
        return String.valueOf(code);
    }

    /**
     * Cleanup expired OTPs (scheduled task)
     * Runs every hour
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredOtps() {
        log.info("Running cleanup task for expired OTPs");
        otpCodeRepository.deleteExpiredOtps(Instant.now());
    }
}