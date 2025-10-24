package com.example.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * User Profile Entity
 * Stores user information and preferences (like Udemy)
 * Separated from authentication data in auth-service
 */
@Entity
@Table(name = "user_profiles", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id", unique = true, nullable = false)
    private String userId; // References user ID from auth-service

    // Profile Information
    @Column(length = 100)
    private String displayName;

    @Column(length = 500)
    private String bio;

    @Column(length = 200)
    private String headline; // e.g., "Senior Java Developer at Google"

    @Column(length = 500)
    private String avatarUrl;

    // Contact & Social
    @Column(length = 100)
    private String website;

    @Column(length = 100)
    private String twitter;

    @Column(length = 100)
    private String linkedin;

    @Column(length = 100)
    private String github;

    // Preferences
    @Column(length = 10)
    private String language; // e.g., "en", "vi"

    @Column(length = 50)
    private String timezone; // e.g., "Asia/Ho_Chi_Minh"

    @Column(nullable = false)
    @Builder.Default
    private Boolean emailNotifications = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean publicProfile = true;

    // Teaching/Learning Stats (for Udemy-like platform)
    @Column(nullable = false)
    @Builder.Default
    private Integer coursesEnrolled = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer coursesCompleted = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer coursesCreated = 0; // For instructors

    // Timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}