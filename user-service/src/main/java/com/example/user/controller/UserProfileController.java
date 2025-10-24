package com.example.user.controller;

import com.example.common.dto.ApiResponse;
import com.example.common.dto.PageResponse;
import com.example.common.security.annotation.CurrentUser;
import com.example.user.dto.ProfileCreateRequest;
import com.example.user.dto.ProfileResponse;
import com.example.user.dto.ProfileUpdateRequest;
import com.example.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * User Profile Controller
 * REST endpoints for user profile management
 */
@Slf4j
@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService profileService;

    /**
     * GET /profiles/me
     * Get current user's profile
     * Access: Requires authentication
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> getCurrentUserProfile(
            @CurrentUser String userId) {

        log.info("Get current user profile request: {}", userId);

        ProfileResponse profile = profileService.getProfileByUserId(userId);

        return ResponseEntity.ok(
                ApiResponse.success(profile, "Profile retrieved successfully")
        );
    }

    /**
     * GET /profiles/user/{userId}
     * Get profile by userId (from auth-service)
     * Access: Public profiles visible to all, private only to owner or ADMIN
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfileByUserId(
            @PathVariable String userId,
            @CurrentUser String currentUserId) {

        log.info("Get profile by userId request: {} by user: {}", userId, currentUserId);

        ProfileResponse profile = profileService.getProfileByUserId(userId);

        // Check if profile is private and user is not owner or admin
        if (!profile.getPublicProfile() && !userId.equals(currentUserId)) {
            log.warn("Unauthorized access attempt to private profile: {} by user: {}", userId, currentUserId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("This profile is private"));
        }

        return ResponseEntity.ok(
                ApiResponse.success(profile, "Profile retrieved successfully")
        );
    }

    /**
     * GET /profiles/{id}
     * Get profile by profile ID
     * Access: Public profiles visible to all, private only to owner or ADMIN
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfileById(
            @PathVariable String id,
            @CurrentUser String currentUserId) {

        log.info("Get profile by ID request: {}", id);

        ProfileResponse profile = profileService.getProfileById(id);

        // Check if profile is private and user is not owner
        if (!profile.getPublicProfile() && !profile.getUserId().equals(currentUserId)) {
            log.warn("Unauthorized access attempt to private profile: {} by user: {}", id, currentUserId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("This profile is private"));
        }

        return ResponseEntity.ok(
                ApiResponse.success(profile, "Profile retrieved successfully")
        );
    }

    /**
     * GET /profiles
     * Get all public profiles with pagination
     * Access: Requires authentication
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProfileResponse>>> getAllPublicProfiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {

        log.info("Get all public profiles request - page: {}, size: {}", page, size);

        Sort sort = sortOrder.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<ProfileResponse> profiles = profileService.getAllPublicProfiles(pageable);

        return ResponseEntity.ok(
                ApiResponse.success(profiles, "Public profiles retrieved successfully")
        );
    }

    /**
     * POST /profiles/me
     * Create profile for current user
     * Access: Requires authentication
     */
    @PostMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> createProfile(
            @CurrentUser String userId,
            @Valid @RequestBody ProfileCreateRequest request) {

        log.info("Create profile request for userId: {}", userId);

        ProfileResponse profile = profileService.createProfile(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(profile, "Profile created successfully"));
    }

    /**
     * PUT /profiles/me
     * Update current user's profile
     * Access: Requires authentication
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateCurrentUserProfile(
            @CurrentUser String userId,
            @Valid @RequestBody ProfileUpdateRequest request) {

        log.info("Update profile request for userId: {}", userId);

        ProfileResponse profile = profileService.updateProfile(userId, request);

        return ResponseEntity.ok(
                ApiResponse.success(profile, "Profile updated successfully")
        );
    }

    /**
     * DELETE /profiles/me
     * Delete current user's profile
     * Access: Requires authentication
     */
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteCurrentUserProfile(
            @CurrentUser String userId) {

        log.info("Delete profile request for userId: {}", userId);

        profileService.deleteProfile(userId);

        return ResponseEntity.ok(
                ApiResponse.success("Profile deleted successfully")
        );
    }

    /**
     * DELETE /profiles/user/{userId}
     * Delete profile by userId
     * Access: ADMIN only
     */
    @DeleteMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProfileByUserId(
            @PathVariable String userId) {

        log.info("Admin delete profile request for userId: {}", userId);

        profileService.deleteProfile(userId);

        return ResponseEntity.ok(
                ApiResponse.success("Profile deleted successfully")
        );
    }

    /**
     * GET /profiles/health
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("User Profile service is running")
        );
    }
}