package com.facem_bani_inc.daily_history_server.model.dto;

import java.util.List;

public record QuizQuestionResponseDTO(
        String questionKey,
        String question,
        String explanation,
        List<QuizOptionResponseDTO> options
) {
}
