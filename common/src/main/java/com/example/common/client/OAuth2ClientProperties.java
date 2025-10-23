package com.example.common.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OAuth2 Client Configuration Properties
 * Each service should configure its own credentials
 */
@Data
@Component
@ConfigurationProperties(prefix = "oauth2.client")
public class OAuth2ClientProperties {

    /**
     * Auth service URL
     * Example: http://localhost:8081
     */
    private String authServiceUrl;

    /**
     * OAuth2 token endpoint
     * Example: /oauth/token
     */
    private String tokenEndpoint = "/oauth/token";

    /**
     * Client ID (service identifier)
     * Example: "test-service", "user-service"
     */
    private String clientId;

    /**
     * Client secret
     */
    private String clientSecret;

    /**
     * Default scope for service tokens
     * Example: "user:read user:write"
     */
    private String scope;

    /**
     * Get full token URL
     */
    public String getTokenUrl() {
        return authServiceUrl + tokenEndpoint;
    }
}