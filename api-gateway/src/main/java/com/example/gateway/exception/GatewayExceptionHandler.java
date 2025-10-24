package com.example.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler for API Gateway
 *
 * Handles all exceptions and converts them to standardized JSON error responses
 */
@Slf4j
@Order(-2)
@Component
@RequiredArgsConstructor
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Gateway error: {}", ex.getMessage(), ex);

        // Determine HTTP status
        HttpStatus status = determineHttpStatus(ex);

        // Build error response
        Map<String, Object> errorResponse = buildErrorResponse(ex, status, exchange);

        // Set response status and content type
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error serializing error response: {}", e.getMessage());
            return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse().bufferFactory()
                            .wrap(getFallbackErrorResponse().getBytes(StandardCharsets.UTF_8)))
            );
        }
    }

    /**
     * Determine HTTP status from exception
     */
    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException rse) {
            return HttpStatus.resolve(rse.getStatusCode().value());
        }

        if (ex instanceof org.springframework.web.server.ServerWebInputException) {
            return HttpStatus.BAD_REQUEST;
        }

        if (ex.getMessage() != null) {
            String message = ex.getMessage().toLowerCase();
            if (message.contains("connection refused") || message.contains("unavailable")) {
                return HttpStatus.SERVICE_UNAVAILABLE;
            }
            if (message.contains("timeout")) {
                return HttpStatus.GATEWAY_TIMEOUT;
            }
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * Build standardized error response
     */
    private Map<String, Object> buildErrorResponse(Throwable ex, HttpStatus status, ServerWebExchange exchange) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);

        Map<String, Object> error = new HashMap<>();
        error.put("code", status.name());
        error.put("message", getErrorMessage(ex, status));

        response.put("error", error);
        response.put("timestamp", Instant.now().toString());
        response.put("path", exchange.getRequest().getPath().value());

        return response;
    }

    /**
     * Get user-friendly error message
     */
    private String getErrorMessage(Throwable ex, HttpStatus status) {
        if (ex instanceof ResponseStatusException rse) {
            return rse.getReason() != null ? rse.getReason() : status.getReasonPhrase();
        }

        return switch (status) {
            case SERVICE_UNAVAILABLE -> "Service temporarily unavailable. Please try again later.";
            case GATEWAY_TIMEOUT -> "Request timeout. Please try again.";
            case BAD_REQUEST -> "Invalid request format.";
            default -> "An unexpected error occurred.";
        };
    }

    /**
     * Fallback error response if JSON serialization fails
     */
    private String getFallbackErrorResponse() {
        return "{\"success\":false,\"error\":{\"code\":\"INTERNAL_SERVER_ERROR\"," +
                "\"message\":\"An unexpected error occurred\"},\"timestamp\":\"" +
                Instant.now() + "\"}";
    }
}