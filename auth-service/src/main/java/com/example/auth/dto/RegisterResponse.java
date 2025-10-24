package com.example.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Register Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {

    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;

    /**
     * Indicates if email verification is needed
     */
    @Builder.Default
    private boolean needsVerification = true;

    private String message;
}