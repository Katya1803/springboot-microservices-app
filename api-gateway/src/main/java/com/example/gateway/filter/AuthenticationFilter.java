package com.example.gateway.filter;

import com.example.common.constant.SecurityConstants;
import com.example.common.security.jwt.JwtTokenValidator;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Authentication Filter
 * Validates JWT token and injects user context headers
 */
@Slf4j
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private JwtTokenValidator jwtTokenValidator;

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Extract token from Authorization header
            String authHeader = request.getHeaders().getFirst(SecurityConstants.JWT_HEADER);

            if (authHeader == null || !authHeader.startsWith(SecurityConstants.JWT_PREFIX)) {
                log.warn("Missing or invalid Authorization header");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token");
            }

            String token = authHeader.replace(SecurityConstants.JWT_PREFIX, "");

            try {
                // Validate token
                jwtTokenValidator.validateToken(token);

                // Extract JTI for blacklist check
                String jti = jwtTokenValidator.getJti(token);
                String blacklistKey = SecurityConstants.REDIS_BLACKLIST_PREFIX + jti;

                // Check if token is blacklisted (reactive)
                return reactiveRedisTemplate.hasKey(blacklistKey)
                        .flatMap(isBlacklisted -> {
                            if (Boolean.TRUE.equals(isBlacklisted)) {
                                log.warn("Token is blacklisted: {}", jti);
                                return Mono.error(new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Token has been revoked"
                                ));
                            }

                            // Extract user information
                            Claims claims = jwtTokenValidator.getClaims(token);
                            String userId = claims.getSubject();
                            List<String> roles = jwtTokenValidator.getRoles(token);
                            String rolesString = String.join(",", roles);

                            // Inject headers
                            ServerHttpRequest modifiedRequest = request.mutate()
                                    .header(SecurityConstants.HEADER_USER_ID, userId)
                                    .header(SecurityConstants.HEADER_USER_ROLES, rolesString)
                                    .build();

                            log.debug("Authenticated request for user: {}, roles: {}", userId, rolesString);

                            // Continue filter chain with modified request
                            return chain.filter(exchange.mutate().request(modifiedRequest).build());
                        });

            } catch (Exception e) {
                log.error("Token validation failed", e);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token: " + e.getMessage());
            }
        };
    }

    public static class Config {
        // Configuration properties if needed
    }
}