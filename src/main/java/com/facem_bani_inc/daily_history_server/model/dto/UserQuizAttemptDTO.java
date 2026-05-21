package com.facem_bani_inc.daily_history_server.model.dto;

import java.time.LocalDateTime;

public record UserQuizAttemptDTO(
        Long id,
        Long eventId,
        int correctAnswers,
        int totalQuestions,
        int xpEarned,
        boolean perfectScore,
        LocalDateTime completedAt
) {
}
