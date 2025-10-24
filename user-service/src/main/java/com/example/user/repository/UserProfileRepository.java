package com.example.user.repository;

import com.example.user.entity.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User Profile Repository
 * JPA repository for UserProfile entity operations
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {

    /**
     * Find profile by userId (from auth-service)
     */
    Optional<UserProfile> findByUserId(String userId);

    /**
     * Check if profile exists for userId
     */
    boolean existsByUserId(String userId);

    /**
     * Find all public profiles with pagination
     */
    @Query("SELECT p FROM UserProfile p WHERE p.publicProfile = true")
    Page<UserProfile> findAllPublicProfiles(Pageable pageable);

    /**
     * Delete profile by userId
     */
    void deleteByUserId(String userId);
}