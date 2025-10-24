package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * User Entity
 * Stores user credentials and profile information
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_username", columnList = "username"),
        @Index(name = "idx_email", columnList = "email")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password; // BCrypt encoded

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(nullable = false)
    private String roles; // Comma-separated: ROLE_ADMIN,ROLE_USER

    /**
     * User Status:
     * - PENDING: Registered but not verified email
     * - ACTIVE: Email verified, can login
     * - INACTIVE: Deactivated
     * - LOCKED: Account locked
     */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING"; // PENDING, ACTIVE, INACTIVE, LOCKED

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "token_version", nullable = false)
    @Builder.Default
    private Integer tokenVersion = 1; // For bulk token revocation

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * Check if user is active and can login
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    /**
     * Check if user is pending email verification
     */
    public boolean isPending() {
        return "PENDING".equals(status);
    }

    /**
     * Get roles as array
     */
    public String[] getRolesArray() {
        return roles != null ? roles.split(",") : new String[0];
    }

    /**
     * Activate user after email verification
     */
    public void activate() {
        this.status = "ACTIVE";
        this.emailVerified = true;
        this.updatedAt = Instant.now();
    }
}