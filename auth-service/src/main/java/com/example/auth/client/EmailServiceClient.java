package com.example.auth.client;

import com.example.common.dto.ApiResponse;
import com.example.common.dto.EmailRequest;
import com.example.common.dto.EmailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Email Service Feign Client
 * For service-to-service email sending
 */
@FeignClient(
        name = "email-service",
        configuration = EmailServiceFeignConfig.class
)
public interface EmailServiceClient {

    @PostMapping("/emails/send")
    ApiResponse<EmailResponse> sendEmail(@RequestBody EmailRequest request);
}