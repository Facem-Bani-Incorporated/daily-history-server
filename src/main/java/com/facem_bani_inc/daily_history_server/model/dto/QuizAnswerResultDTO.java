package com.facem_bani_inc.daily_history_server.model.dto;

public record QuizAnswerResultDTO(
        String questionKey,
        String selectedOptionId,
        String correctOptionId,
        boolean isCorrect
) {
}
