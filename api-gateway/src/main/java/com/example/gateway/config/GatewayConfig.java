package com.example.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway Route Configuration
 *
 * Defines routing rules for incoming requests to microservices
 *
 * Routes:
 * - /auth/** → auth-service (public)
 * - /api/users/** → user-service (protected)
 * - /api/test/** → test-service (protected)
 */
@Slf4j
@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        log.info("Configuring Gateway routes");

        return builder.routes()
                // ========================================
                // Auth Service Routes (Public)
                // ========================================
                .route("auth-service", r -> r
                        .path("/auth/**")
                        .filters(f -> f
                                .stripPrefix(0)  // Keep /auth prefix
                                .retry(config -> config
                                        .setRetries(3)
                                        .setMethods(org.springframework.http.HttpMethod.GET,
                                                org.springframework.http.HttpMethod.POST)
                                )
                        )
                        .uri("lb://auth-service")
                )

                // ========================================
                // User Service Routes (Protected)
                // ========================================
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f
                                .stripPrefix(2)  // Remove /api/users
                                .retry(config -> config.setRetries(2))
                        )
                        .uri("lb://user-service")
                )

                // ========================================
                // Test Service Routes (Mixed: public + protected)
                // ========================================
                .route("test-service", r -> r
                        .path("/api/test/**")
                        .filters(f -> f
                                .stripPrefix(1)  // Remove /api, keep /test
                                .retry(config -> config.setRetries(2))
                        )
                        .uri("lb://test-service")
                )

                .build();
    }
}