package com.facem_bani_inc.daily_history_server.model.dto;

import java.util.List;

public record QuizSubmitDTO(
        String language,
        List<QuizAnswerDTO> answers
) {
}
