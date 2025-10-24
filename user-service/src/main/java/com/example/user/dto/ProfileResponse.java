package com.example.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Profile Response DTO
 * Returns user profile information to clients
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileResponse {

    private String id;
    private String userId;

    // Profile Information
    private String displayName;
    private String bio;
    private String headline;
    private String avatarUrl;

    // Contact & Social
    private String website;
    private String twitter;
    private String linkedin;
    private String github;

    // Preferences
    private String language;
    private String timezone;
    private Boolean emailNotifications;
    private Boolean publicProfile;

    // Stats
    private Integer coursesEnrolled;
    private Integer coursesCompleted;
    private Integer coursesCreated;

    // Timestamps
    private Instant createdAt;
    private Instant updatedAt;
}