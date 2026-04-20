package com.facem_bani_inc.daily_history_server.model.dto;

public record UserProfileDTO(
        Long id,
        String username,
        String email,
        String avatarUrl,
        boolean isPro
) {
}
