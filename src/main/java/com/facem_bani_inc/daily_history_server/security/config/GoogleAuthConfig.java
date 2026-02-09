package com.facem_bani_inc.daily_history_server.security.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class GoogleAuthConfig {

    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier(@Value("${google.oauth.client-ids}") String clientIds) {
        List<String> audience = Arrays.stream(clientIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        HttpTransport transport = new ApacheHttpTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        return new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(audience)
                .build();
    }
}