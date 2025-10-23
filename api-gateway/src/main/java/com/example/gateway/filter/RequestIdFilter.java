// api-gateway/src/main/java/com/example/gateway/filter/RequestIdFilter.java
package com.example.gateway.filter;

import com.example.common.constant.SecurityConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Request ID Filter
 * Adds unique request ID to all requests for tracing
 */
@Slf4j
@Component
public class RequestIdFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Check if request ID already exists
        String requestId = request.getHeaders().getFirst(SecurityConstants.HEADER_REQUEST_ID);

        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        // Add request ID to request headers
        ServerHttpRequest modifiedRequest = request.mutate()
                .header(SecurityConstants.HEADER_REQUEST_ID, requestId)
                .build();

        // Add request ID to response headers for client
        exchange.getResponse().getHeaders().add(SecurityConstants.HEADER_REQUEST_ID, requestId);

        log.debug("Request ID: {} - {} {}", requestId, request.getMethod(), request.getPath());

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // Execute first
    }
}