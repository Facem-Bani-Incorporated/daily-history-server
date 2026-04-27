package com.facem_bani_inc.daily_history_server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class RevenueCatService {

    private static final String PRO_ENTITLEMENT = "Daily History Pro";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public RevenueCatService(
            @Value("${revenuecat.secret-key}") String secretKey,
            RestClient.Builder builder,
            ObjectMapper objectMapper) {
        this.restClient = builder
                .baseUrl("https://api.revenuecat.com/v1")
                .defaultHeader("Authorization", "Bearer " + secretKey)
                .build();
        this.objectMapper = objectMapper;
    }

    public boolean isUserPro(Long userId) {
        try {
            String body = restClient.get()
                    .uri("/subscribers/" + userId)
                    .retrieve()
                    .body(String.class);
            JsonNode active = objectMapper.readTree(body)
                    .path("subscriber").path("entitlements").path("active");
            return active.has(PRO_ENTITLEMENT);
        } catch (Exception e) {
            log.error("RevenueCat entitlement check failed for user {}: {}", userId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Subscription verification temporarily unavailable");
        }
    }
}
