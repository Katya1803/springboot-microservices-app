package com.example.gateway.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Rate Limit Filter
 * Redis-based rate limiting per IP and endpoint
 */
@Slf4j
@Component
public class RateLimitFilter extends AbstractGatewayFilterFactory<RateLimitFilter.Config> {

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    public RateLimitFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String clientIp = getClientIp(exchange);
            String path = exchange.getRequest().getPath().value();
            String rateLimitKey = RATE_LIMIT_PREFIX + clientIp + ":" + path;

            return reactiveRedisTemplate.opsForValue()
                    .increment(rateLimitKey)
                    .flatMap(count -> {
                        if (count == 1) {
                            // First request, set expiration
                            return reactiveRedisTemplate.expire(rateLimitKey, Duration.ofSeconds(config.getDuration()))
                                    .flatMap(success -> processRequest(count, config, exchange, chain));
                        } else {
                            return processRequest(count, config, exchange, chain);
                        }
                    })
                    .onErrorResume(e -> {
                        log.error("Rate limit check failed", e);
                        // On error, allow request (fail open)
                        return chain.filter(exchange);
                    });
        };
    }

    private Mono<Void> processRequest(Long count, Config config,
                                      org.springframework.web.server.ServerWebExchange exchange,
                                      org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        if (count > config.getLimit()) {
            log.warn("Rate limit exceeded for IP: {}, path: {}, count: {}",
                    getClientIp(exchange),
                    exchange.getRequest().getPath().value(),
                    count);

            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(config.getLimit()));
            exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", "0");

            return exchange.getResponse().setComplete();
        }

        // Add rate limit headers
        exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(config.getLimit()));
        exchange.getResponse().getHeaders().add("X-RateLimit-Remaining",
                String.valueOf(config.getLimit() - count));

        return chain.filter(exchange);
    }

    private String getClientIp(org.springframework.web.server.ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }

    @Data
    public static class Config {
        private int limit = 60;         // Max requests
        private int duration = 60;      // Time window in seconds
    }
}