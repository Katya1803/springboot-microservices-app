package com.example.auth.client;

import com.example.common.feign.OAuth2FeignRequestInterceptor;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Email Service Feign Configuration
 * Auto-inject service token for email-service calls
 */
@Configuration
@RequiredArgsConstructor
public class EmailServiceFeignConfig {

    private final EmailServiceTokenProvider tokenProvider;

    @Bean
    public RequestInterceptor emailServiceRequestInterceptor() {
        return new OAuth2FeignRequestInterceptor(tokenProvider, "email-service");
    }
}