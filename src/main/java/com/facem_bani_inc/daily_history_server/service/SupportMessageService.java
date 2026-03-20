package com.facem_bani_inc.daily_history_server.service;

import com.facem_bani_inc.daily_history_server.entity.SupportMessage;
import com.facem_bani_inc.daily_history_server.entity.User;
import com.facem_bani_inc.daily_history_server.model.dto.SupportMessageRequest;
import com.facem_bani_inc.daily_history_server.repository.SupportMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SupportMessageService {

    private final SupportMessageRepository supportMessageRepository;
    private final UserService userService;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${app.support.to}")
    private String adminEmail;

    public void saveAndSendMessage(SupportMessageRequest request) {
        User authenticatedUser = userService.getAuthenticatedUser();
        SupportMessage supportMessage = new SupportMessage();
        supportMessage.setUser(authenticatedUser);
        supportMessage.setCategory(request.category());
        supportMessage.setSubject(request.subject().trim());
        supportMessage.setMessage(request.message().trim());
        SupportMessage savedMessage = supportMessageRepository.save(supportMessage);

        try {
            sendEmail(savedMessage);
        } catch (MailException e) {
            log.error("Failed to send support email for message id {}", savedMessage.getId(), e);
        }
    }

    private void sendEmail(SupportMessage savedMessage) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(senderEmail);
        mailMessage.setTo(adminEmail);
        mailMessage.setReplyTo(savedMessage.getUser().getEmail());
        mailMessage.setSubject("Support message: " + savedMessage.getSubject());
        mailMessage.setText("""
                New message from Daily History.

                Username: %s
                Email: %s
                Category: %s
                Date: %s

                Message:
                %s
                """.formatted(
                savedMessage.getUser().getUsername(),
                savedMessage.getUser().getEmail(),
                savedMessage.getCategory(),
                savedMessage.getCreatedAt().toLocalDate(),
                savedMessage.getMessage()
        ));
        mailSender.send(mailMessage);
        log.info("Support email sent successfully to {}", adminEmail);
    }
}
