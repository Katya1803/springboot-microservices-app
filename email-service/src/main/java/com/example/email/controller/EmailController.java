package com.example.email.controller;

import com.example.common.dto.ApiResponse;
import com.example.common.dto.EmailRequest;
import com.example.common.dto.EmailResponse;
import com.example.email.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Email Controller
 * REST API for sending emails (service-to-service only)
 */
@Slf4j
@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    /**
     * Send email
     * POST /emails/send
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<EmailResponse>> sendEmail(
            @Valid @RequestBody EmailRequest request) {

        log.info("Received email request for: {}", request.getTo());

        EmailResponse response = emailService.sendEmail(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(response, "Email sent successfully"));
        } else {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("EMAIL_SEND_FAILED: "+ response.getMessage()));
        }
    }

    /**
     * Health check
     * GET /emails/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Email service is running"));
    }
}