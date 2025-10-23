package com.example.auth.service;

import com.example.auth.dto.OAuth2TokenRequest;
import com.example.auth.dto.OAuth2TokenResponse;
import com.example.auth.entity.ServiceClient;
import com.example.auth.repository.jpa.ServiceClientRepository;
import com.example.common.constant.ErrorCode;
import com.example.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * OAuth2 Client Service
 * Handles service-to-service authentication (Client Credentials flow)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2ClientService {

    private final ServiceClientRepository serviceClientRepository;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final PasswordEncoder passwordEncoder;

    /**
     * Generate service token using client credentials
     */
    public OAuth2TokenResponse generateServiceToken(OAuth2TokenRequest request) {
        // Validate grant type
        if (!"client_credentials".equals(request.getGrantType())) {
            throw new UnauthorizedException("Invalid grant type. Only 'client_credentials' is supported");
        }

        // Find service client
        ServiceClient client = serviceClientRepository.findByClientId(request.getClientId())
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.INVALID_CLIENT.getMessage()));

        // Check if client is enabled
        if (!client.getEnabled()) {
            throw new UnauthorizedException("Service client is disabled");
        }

        // Verify client secret
        if (!passwordEncoder.matches(request.getClientSecret(), client.getClientSecret())) {
            throw new UnauthorizedException("Invalid client credentials");
        }

        // Validate scope
        String requestedScope = request.getScope() != null ? request.getScope() : client.getAllowedScopes();
        if (!isValidScope(client, requestedScope)) {
            throw new UnauthorizedException("Requested scope is not allowed for this client");
        }

        // Validate audience (optional but recommended)
        String audience = request.getAudience() != null ? request.getAudience() : "default";

        // Generate service token
        String accessToken = jwtTokenGenerator.generateServiceToken(
                client.getClientId(),
                audience,
                requestedScope
        );

        log.info("Generated service token for client: {}, audience: {}",
                client.getClientId(), audience);

        return OAuth2TokenResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenGenerator.getServiceTokenExpirationSeconds())
                .scope(requestedScope)
                .build();
    }

    /**
     * Validate if requested scope is allowed for client
     */
    private boolean isValidScope(ServiceClient client, String requestedScope) {
        if (requestedScope == null || requestedScope.isEmpty()) {
            return true;
        }

        String[] requestedScopes = requestedScope.split(",");
        for (String scope : requestedScopes) {
            if (!client.hasScope(scope.trim())) {
                log.warn("Client {} requested invalid scope: {}", client.getClientId(), scope);
                return false;
            }
        }
        return true;
    }
}