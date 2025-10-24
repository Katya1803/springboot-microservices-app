package com.example.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Rate Limit Configuration Properties
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    /**
     * Default rate limit (requests per duration)
     */
    private Integer defaultLimit = 100;

    /**
     * Default time window in seconds
     */
    private Integer defaultDuration = 60;

    /**
     * Auth login endpoint rate limit
     */
    private Integer authLoginLimit = 5;

    /**
     * Auth login time window in seconds
     */
    private Integer authLoginDuration = 900; // 15 minutes
}