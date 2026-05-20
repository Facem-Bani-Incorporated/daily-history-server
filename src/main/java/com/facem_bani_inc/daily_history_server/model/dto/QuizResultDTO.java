package com.facem_bani_inc.daily_history_server.model.dto;

import java.util.List;

public record QuizResultDTO(
        int correctAnswers,
        int totalQuestions,
        int xpEarned,
        boolean perfectScore,
        List<QuizAnswerResultDTO> answerResults
) {
}
