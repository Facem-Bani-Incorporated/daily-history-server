package com.facem_bani_inc.daily_history_server.model.dto;

public record GamificationSyncDTO(
        Integer totalXP,
        Integer currentStreak,
        Integer longestStreak,
        Integer totalEventsRead,
        Integer dailyGoalsCompleted,
        String lastActiveDate,
        String gamificationData
) {
}
