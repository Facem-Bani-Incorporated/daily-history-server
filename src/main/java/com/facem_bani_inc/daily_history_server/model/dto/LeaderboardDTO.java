package com.facem_bani_inc.daily_history_server.model.dto;

public record LeaderboardDTO(
        Long userId,
        String username,
        Integer totalXP,
        Integer currentStreak,
        Integer totalEventsRead,
        Integer dailyGoalsCompleted
) {}