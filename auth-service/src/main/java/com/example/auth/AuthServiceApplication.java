package com.example.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.example.auth",
        "com.example.common"
})
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = "com.example.auth.repository.jpa")
@EnableRedisRepositories(basePackages = "com.example.auth.repository.redis")
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}