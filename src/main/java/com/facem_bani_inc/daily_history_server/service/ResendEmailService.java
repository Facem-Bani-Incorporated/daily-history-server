package com.facem_bani_inc.daily_history_server.service;

import com.facem_bani_inc.daily_history_server.config.ResendProperties;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResendEmailService {

    private final ResendProperties resendProperties;

    public void sendEmail(String to, String subject, String html) throws ResendException {
        Resend resend = new Resend(resendProperties.apiKey());
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(resendProperties.fromEmail())
                .to(to)
                .subject(subject)
                .html(html)
                .build();

        CreateEmailResponse response = resend.emails().send(params);
        log.info("Resend email sent successfully. To: {}, Subject: {}, EmailId: {}", to, subject, response.getId());
    }
}
