package com.facem_bani_inc.daily_history_server.model.dto;

public record QuizStatusDTO(
        Long eventId,
        boolean attempted,
        Integer correctAnswers,
        Integer totalQuestions,
        Integer xpEarned
) {
}
