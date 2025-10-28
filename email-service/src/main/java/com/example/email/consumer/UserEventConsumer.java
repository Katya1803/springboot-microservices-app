package com.example.email.consumer;

import com.example.common.constant.KafkaTopics;
import com.example.common.dto.EmailRequest;
import com.example.common.event.UserVerifiedEvent;
import com.example.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final EmailService emailService;

    @KafkaListener(
            topics = KafkaTopics.USER_EVENTS,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleUserVerifiedEvent(
            @Payload UserVerifiedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) String partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("Received UserVerifiedEvent: userId={}, eventId={}, partition={}, offset={}",
                event.getUserId(), event.getEventId(), partition, offset);

        try {
            sendWelcomeEmail(event);
            log.info("✅ Welcome email sent successfully for user: {}", event.getEmail());
        } catch (Exception e) {
            log.error("❌ Failed to send welcome email for user: {}", event.getEmail(), e);
        }
    }

    private void sendWelcomeEmail(UserVerifiedEvent event) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", event.getFirstName());
        variables.put("username", event.getUsername());
        variables.put("email", event.getEmail());

        EmailRequest emailRequest = EmailRequest.builder()
                .to(event.getEmail())
                .subject("Welcome to Our Platform! 🎉")
                .template("welcome-email")
                .variables(variables)
                .build();

        emailService.sendEmail(emailRequest);
    }

}
