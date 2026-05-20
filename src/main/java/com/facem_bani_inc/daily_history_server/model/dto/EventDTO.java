package com.facem_bani_inc.daily_history_server.model.dto;

import com.facem_bani_inc.daily_history_server.model.enums.ECategory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record EventDTO(
        Long id,
        ECategory category,
        TranslationDTO titleTranslations,
        TranslationDTO narrativeTranslations,
        LocalDate eventDate,
        Double impactScore,
        String sourceUrl,
        Integer pageViews30d,
        boolean isPro,
        String location,
        List<String> gallery,
        Map<String, List<QuizQuestionInputDTO>> quiz
) {
}
