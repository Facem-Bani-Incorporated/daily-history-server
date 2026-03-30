package com.facem_bani_inc.daily_history_server.service;

import com.facem_bani_inc.daily_history_server.entity.SupportMessage;
import com.facem_bani_inc.daily_history_server.entity.User;
import com.facem_bani_inc.daily_history_server.model.dto.SupportMessageRequest;
import com.facem_bani_inc.daily_history_server.repository.SupportMessageRepository;
import com.resend.core.exception.ResendException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SupportMessageService {

    private final SupportMessageRepository supportMessageRepository;
    private final ResendEmailService resendEmailService;
    private final UserService userService;

    @Value("${app.support.to}")
    private String adminEmail;

    public void saveAndSendMessage(SupportMessageRequest request) {
        User authenticatedUser = userService.getAuthenticatedUser();
        SupportMessage supportMessage = new SupportMessage();
        supportMessage.setUser(authenticatedUser);
        supportMessage.setCategory(request.category());
        supportMessage.setSubject(request.subject());
        supportMessage.setMessage(request.message());
        SupportMessage savedMessage = supportMessageRepository.save(supportMessage);

        try {
            sendEmail(savedMessage);
        } catch (MailException | ResendException e) {
            log.error("Failed to send support email for message id {}", savedMessage.getId(), e);
        }
    }

    public void sendEmail(SupportMessage savedMessage) throws ResendException {
        String html = """
                <h2>New support message from Daily History</h2>
                <p><b>Username:</b> %s</p>
                <p><b>Email:</b> %s</p>
                <p><b>Category:</b> %s</p>
                <p><b>Message:</b></p>
                <p>%s</p>
                """
                .formatted(
                        savedMessage.getUser().getUsername(),
                        savedMessage.getUser().getEmail(),
                        savedMessage.getCategory(),
                        savedMessage.getMessage()
                );

        resendEmailService.sendEmail(adminEmail, "Support message: " + savedMessage.getSubject(), html);
    }
}
