package com.example.auth.controller;

import com.example.auth.dto.OAuth2TokenRequest;
import com.example.auth.dto.OAuth2TokenResponse;
import com.example.auth.service.OAuth2ClientService;
import com.example.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * OAuth2 Token Controller
 * Handles service-to-service authentication (Client Credentials flow)
 */
@Slf4j
@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class OAuth2TokenController {

    private final OAuth2ClientService oauth2ClientService;

    /**
     * Generate service token
     * POST /oauth/token
     *
     * This endpoint is for service-to-service authentication only
     * It should NOT be exposed via API Gateway
     */
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<OAuth2TokenResponse>> getToken(
            @Valid @RequestBody OAuth2TokenRequest request) {

        log.info("Service token request from client: {}", request.getClientId());

        OAuth2TokenResponse response = oauth2ClientService.generateServiceToken(request);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Service token generated successfully")
        );
    }

    /**
     * Health check for OAuth2 endpoint
     * GET /oauth/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("OAuth2 service is running")
        );
    }
}