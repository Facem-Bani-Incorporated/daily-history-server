package com.facem_bani_inc.daily_history_server.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RevenueCatWebhookPayload(
        @JsonProperty("event") RevenueCatEvent event
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RevenueCatEvent(
            @JsonProperty("type") String type,
            @JsonProperty("app_user_id") String appUserId,
            @JsonProperty("transferred_from") List<String> transferredFrom,
            @JsonProperty("transferred_to") List<String> transferredTo
    ) {}
}
