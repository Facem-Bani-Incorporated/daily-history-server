package com.facem_bani_inc.daily_history_server.model.dto;

import java.util.List;

public record QuizResponseDTO(
        Long eventId,
        String language,
        List<QuizQuestionResponseDTO> questions
) {
}
