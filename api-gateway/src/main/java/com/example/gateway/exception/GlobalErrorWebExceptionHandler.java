package com.example.gateway.exception;

import com.example.common.constant.ErrorCode;
import com.example.common.dto.ApiResponse;
import com.example.common.dto.ErrorResponse;
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

/**
 * Global Error Handler for Gateway
 */
@Slf4j
@Component
@Order(-1)  // High priority
@RequiredArgsConstructor
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Gateway error", ex);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorCode = ErrorCode.INTERNAL_SERVER_ERROR.getCode();
        String message = "An unexpected error occurred";

        if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.resolve(rse.getStatusCode().value());
            message = rse.getReason();

            if (status == HttpStatus.UNAUTHORIZED) {
                errorCode = ErrorCode.UNAUTHORIZED.getCode();
            } else if (status == HttpStatus.FORBIDDEN) {
                errorCode = ErrorCode.ACCESS_DENIED.getCode();
            } else if (status == HttpStatus.TOO_MANY_REQUESTS) {
                errorCode = ErrorCode.RATE_LIMIT_EXCEEDED.getCode();
                message = "Too many requests. Please try again later";
            }
        }

        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode,
                message,
                exchange.getRequest().getPath().value()
        );

        ApiResponse<ErrorResponse> apiResponse = ApiResponse.error(errorResponse);

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(apiResponse);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Failed to write error response", e);
            return exchange.getResponse().setComplete();
        }
    }
}