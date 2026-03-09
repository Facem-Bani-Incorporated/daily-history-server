package com.facem_bani_inc.daily_history_server.model.dto;

import com.facem_bani_inc.daily_history_server.model.ECategory;

import java.time.LocalDate;
import java.util.List;

public record EventDTO(
        ECategory category,
        TranslationDTO titleTranslations,
        TranslationDTO narrativeTranslations,
        LocalDate eventDate,
        Double impactScore,
        String sourceUrl,
        Integer pageViews30d,
        List<String> gallery
) {
}
