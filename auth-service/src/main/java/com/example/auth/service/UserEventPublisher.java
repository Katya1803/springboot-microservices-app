package com.example.auth.service;

import com.example.auth.entity.User;
import com.example.common.constant.KafkaTopics;
import com.example.common.event.UserVerifiedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventPublisher {
    private final KafkaTemplate<String, UserVerifiedEvent> kafkaTemplate;

    public void publishUserVerifiedEvent(User user) {
        UserVerifiedEvent event = UserVerifiedEvent.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();

        log.info("Publishing UserVerifiedEvent for user: {}", user.getEmail());

        kafkaTemplate.send(KafkaTopics.USER_EVENTS, user.getId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("✅ Event published successfully: userId={}", user.getId());
                    } else {
                        log.error("❌ Failed to publish event: userId={}", user.getId(), ex);
                    }
                });
    }
}
