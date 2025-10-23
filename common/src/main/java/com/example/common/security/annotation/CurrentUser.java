package com.example.common.security.annotation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

/**
 * Annotation to inject current user ID in controller methods
 *
 * Usage:
 * @GetMapping("/me")
 * public UserResponse getCurrentUser(@CurrentUser String userId) {
 *     return userService.getUserById(userId);
 * }
 */
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal(expression = "username")
public @interface CurrentUser {
}