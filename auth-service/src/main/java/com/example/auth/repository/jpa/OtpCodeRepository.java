package com.example.auth.repository.jpa;

import com.example.auth.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

/**
 * OTP Code Repository
 */
@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, String> {

    /**
     * Find valid OTP by email and code
     */
    Optional<OtpCode> findByEmailAndCodeAndVerifiedFalse(String email, String code);

    /**
     * Find latest OTP by email
     */
    Optional<OtpCode> findFirstByEmailOrderByCreatedAtDesc(String email);

    /**
     * Delete expired OTPs
     */
    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.expiresAt < :now")
    void deleteExpiredOtps(Instant now);

    /**
     * Delete all OTPs for email
     */
    void deleteByEmail(String email);
}