package com.example.user.service;

import com.example.common.dto.PageResponse;
import com.example.common.exception.ResourceNotFoundException;
import com.example.user.dto.ProfileCreateRequest;
import com.example.user.dto.ProfileResponse;
import com.example.user.dto.ProfileUpdateRequest;
import com.example.user.entity.UserProfile;
import com.example.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * User Profile Service
 * Business logic for user profile management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository profileRepository;

    /**
     * Get profile by userId (from auth-service)
     */
    @Transactional(readOnly = true)
    public ProfileResponse getProfileByUserId(String userId) {
        log.debug("Getting profile for userId: {}", userId);

        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user: " + userId));

        return mapToResponse(profile);
    }

    /**
     * Get profile by profile ID
     */
    @Transactional(readOnly = true)
    public ProfileResponse getProfileById(String profileId) {
        log.debug("Getting profile by ID: {}", profileId);

        UserProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with id: " + profileId));

        return mapToResponse(profile);
    }

    /**
     * Get all public profiles with pagination
     */
    @Transactional(readOnly = true)
    public PageResponse<ProfileResponse> getAllPublicProfiles(Pageable pageable) {
        log.debug("Getting all public profiles with pagination: {}", pageable);

        Page<UserProfile> profilePage = profileRepository.findAllPublicProfiles(pageable);

        List<ProfileResponse> profiles = profilePage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<ProfileResponse>builder()
                .content(profiles)
                .page(profilePage.getNumber())
                .size(profilePage.getSize())
                .totalElements(profilePage.getTotalElements())
                .totalPages(profilePage.getTotalPages())
                .first(profilePage.isFirst())
                .last(profilePage.isLast())
                .build();
    }

    /**
     * Create new profile for user
     */
    @Transactional
    public ProfileResponse createProfile(String userId, ProfileCreateRequest request) {
        log.info("Creating profile for userId: {}", userId);

        // Check if profile already exists
        if (profileRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("Profile already exists for user: " + userId);
        }

        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .displayName(request.getDisplayName())
                .bio(request.getBio())
                .headline(request.getHeadline())
                .avatarUrl(request.getAvatarUrl())
                .website(request.getWebsite())
                .twitter(request.getTwitter())
                .linkedin(request.getLinkedin())
                .github(request.getGithub())
                .language(request.getLanguage() != null ? request.getLanguage() : "en")
                .timezone(request.getTimezone() != null ? request.getTimezone() : "UTC")
                .emailNotifications(request.getEmailNotifications() != null ? request.getEmailNotifications() : true)
                .publicProfile(request.getPublicProfile() != null ? request.getPublicProfile() : true)
                .build();

        UserProfile savedProfile = profileRepository.save(profile);
        log.info("Profile created successfully for userId: {}", userId);

        return mapToResponse(savedProfile);
    }

    /**
     * Update user profile
     */
    @Transactional
    public ProfileResponse updateProfile(String userId, ProfileUpdateRequest request) {
        log.info("Updating profile for userId: {}", userId);

        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user: " + userId));

        // Update only provided fields
        if (request.getDisplayName() != null) {
            profile.setDisplayName(request.getDisplayName());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getHeadline() != null) {
            profile.setHeadline(request.getHeadline());
        }
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getWebsite() != null) {
            profile.setWebsite(request.getWebsite());
        }
        if (request.getTwitter() != null) {
            profile.setTwitter(request.getTwitter());
        }
        if (request.getLinkedin() != null) {
            profile.setLinkedin(request.getLinkedin());
        }
        if (request.getGithub() != null) {
            profile.setGithub(request.getGithub());
        }
        if (request.getLanguage() != null) {
            profile.setLanguage(request.getLanguage());
        }
        if (request.getTimezone() != null) {
            profile.setTimezone(request.getTimezone());
        }
        if (request.getEmailNotifications() != null) {
            profile.setEmailNotifications(request.getEmailNotifications());
        }
        if (request.getPublicProfile() != null) {
            profile.setPublicProfile(request.getPublicProfile());
        }

        UserProfile savedProfile = profileRepository.save(profile);
        log.info("Profile updated successfully for userId: {}", userId);

        return mapToResponse(savedProfile);
    }

    /**
     * Delete user profile
     */
    @Transactional
    public void deleteProfile(String userId) {
        log.info("Deleting profile for userId: {}", userId);

        if (!profileRepository.existsByUserId(userId)) {
            throw new ResourceNotFoundException("Profile not found for user: " + userId);
        }

        profileRepository.deleteByUserId(userId);
        log.info("Profile deleted successfully for userId: {}", userId);
    }

    /**
     * Map UserProfile entity to ProfileResponse DTO
     */
    private ProfileResponse mapToResponse(UserProfile profile) {
        return ProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .displayName(profile.getDisplayName())
                .bio(profile.getBio())
                .headline(profile.getHeadline())
                .avatarUrl(profile.getAvatarUrl())
                .website(profile.getWebsite())
                .twitter(profile.getTwitter())
                .linkedin(profile.getLinkedin())
                .github(profile.getGithub())
                .language(profile.getLanguage())
                .timezone(profile.getTimezone())
                .emailNotifications(profile.getEmailNotifications())
                .publicProfile(profile.getPublicProfile())
                .coursesEnrolled(profile.getCoursesEnrolled())
                .coursesCompleted(profile.getCoursesCompleted())
                .coursesCreated(profile.getCoursesCreated())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}