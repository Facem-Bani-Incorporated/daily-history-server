package com.facem_bani_inc.daily_history_server.config;

import com.resend.Resend;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResendConfig {

    @Bean
    public Resend resend(ResendProperties properties) {
        return new Resend(properties.apiKey());
    }
}
