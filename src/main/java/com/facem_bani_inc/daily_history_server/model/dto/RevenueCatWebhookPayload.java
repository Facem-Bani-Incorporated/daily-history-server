package com.facem_bani_inc.daily_history_server.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RevenueCatWebhookPayload(
        @JsonProperty("event") RevenueCatEvent event
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RevenueCatEvent(
            @JsonProperty("type") String type,
            @JsonProperty("app_user_id") String appUserId
    ) {}
}
