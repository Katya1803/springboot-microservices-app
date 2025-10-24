package com.example.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Email Request DTO
 * Used for service-to-service email sending
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid email format")
    private String to;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Template name is required")
    private String template;

    /**
     * Template variables (e.g., {"otp": "123456", "firstName": "John"})
     */
    private Map<String, Object> variables;

    /**
     * Optional sender name (default: system)
     */
    private String from;
}