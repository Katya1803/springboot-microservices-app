package com.example.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Profile Update Request DTO
 * Used for updating user profile information
 * All fields are optional - only provided fields will be updated
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileUpdateRequest {

    @Size(min = 1, max = 100, message = "Display name must be between 1 and 100 characters")
    private String displayName;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;

    @Size(max = 200, message = "Headline must not exceed 200 characters")
    private String headline;

    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    private String avatarUrl;

    @Size(max = 100, message = "Website must not exceed 100 characters")
    private String website;

    @Size(max = 100, message = "Twitter handle must not exceed 100 characters")
    private String twitter;

    @Size(max = 100, message = "LinkedIn URL must not exceed 100 characters")
    private String linkedin;

    @Size(max = 100, message = "GitHub username must not exceed 100 characters")
    private String github;

    private String language;
    private String timezone;
    private Boolean emailNotifications;
    private Boolean publicProfile;
}