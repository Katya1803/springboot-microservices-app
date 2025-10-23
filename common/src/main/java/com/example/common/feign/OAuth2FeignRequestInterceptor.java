package com.example.common.feign;

import com.example.common.client.OAuth2TokenProvider;
import com.example.common.constant.SecurityConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Feign Request Interceptor for OAuth2 Service Tokens
 * Automatically injects service token in Authorization header
 *
 * Usage in Feign Client:
 * @FeignClient(name = "user-service", configuration = UserServiceFeignConfig.class)
 * public interface UserServiceClient {
 *     @GetMapping("/users/{id}")
 *     UserResponse getUser(@PathVariable String id);
 * }
 *
 * FeignConfig:
 * @Configuration
 * public class UserServiceFeignConfig {
 *     @Bean
 *     public RequestInterceptor oauth2FeignRequestInterceptor(OAuth2TokenProvider tokenProvider) {
 *         return new OAuth2FeignRequestInterceptor(tokenProvider, "user-service");
 *     }
 * }
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth2FeignRequestInterceptor implements RequestInterceptor {

    private final OAuth2TokenProvider tokenProvider;
    private final String audience;

    @Override
    public void apply(RequestTemplate template) {
        try {
            // Get service token for target audience
            String token = tokenProvider.getServiceToken(audience);

            // Add Authorization header
            template.header(
                    SecurityConstants.JWT_HEADER,
                    SecurityConstants.JWT_PREFIX + token
            );

            log.debug("Added service token for audience: {}", audience);

        } catch (Exception e) {
            log.error("Failed to add service token to Feign request for audience: {}", audience, e);
            throw new RuntimeException("Failed to obtain service token", e);
        }
    }
}