package com.facem_bani_inc.daily_history_server.model.dto;

import com.facem_bani_inc.daily_history_server.model.enums.ESupportCategory;

public record SupportMessageRequest(
        ESupportCategory category,
        String subject,
        String message
) {
}