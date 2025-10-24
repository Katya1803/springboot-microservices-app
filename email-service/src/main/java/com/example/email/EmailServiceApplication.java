package com.example.email;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Email Service Application
 * Handles email sending for OTP and notifications
 */
@SpringBootApplication(scanBasePackages = {
		"com.example.email",
		"com.example.common"
})
@EnableDiscoveryClient
public class EmailServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmailServiceApplication.class, args);
	}
}