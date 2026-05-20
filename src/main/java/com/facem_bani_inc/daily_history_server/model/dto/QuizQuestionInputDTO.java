package com.facem_bani_inc.daily_history_server.model.dto;

import java.util.List;

public record QuizQuestionInputDTO(
        String id,
        String question,
        List<QuizOptionInputDTO> options,
        String correctId,
        String explanation
) {
}
