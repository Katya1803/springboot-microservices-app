package com.example.email.service;

import com.example.common.dto.EmailRequest;
import com.example.common.dto.EmailResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.UUID;

/**
 * Email Service
 * Handles email sending with Thymeleaf templates
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${email.from.address}")
    private String fromAddress;

    @Value("${email.from.name}")
    private String fromName;

    /**
     * Send email using template
     */
    public EmailResponse sendEmail(EmailRequest request) {
        try {
            log.info("Sending email to: {} with template: {}", request.getTo(), request.getTemplate());

            // Create MimeMessage
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Set sender
            String sender = request.getFrom() != null ? request.getFrom() : fromName;
            helper.setFrom(fromAddress, sender);

            // Set recipient
            helper.setTo(request.getTo());

            // Set subject
            helper.setSubject(request.getSubject());

            // Process template with variables
            Context context = new Context();
            if (request.getVariables() != null) {
                request.getVariables().forEach(context::setVariable);
            }
            String htmlContent = templateEngine.process(request.getTemplate(), context);

            // Set content
            helper.setText(htmlContent, true);

            // Send email
            mailSender.send(mimeMessage);

            String emailId = UUID.randomUUID().toString();
            log.info("Email sent successfully to: {} with ID: {}", request.getTo(), emailId);

            return EmailResponse.builder()
                    .success(true)
                    .message("Email sent successfully")
                    .emailId(emailId)
                    .build();

        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", request.getTo(), e);
            return EmailResponse.builder()
                    .success(false)
                    .message("Failed to send email: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error sending email to: {}", request.getTo(), e);
            return EmailResponse.builder()
                    .success(false)
                    .message("Unexpected error: " + e.getMessage())
                    .build();
        }
    }
}