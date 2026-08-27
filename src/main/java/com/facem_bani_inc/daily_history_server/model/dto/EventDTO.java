package com.facem_bani_inc.daily_history_server.model.dto;

import com.facem_bani_inc.daily_history_server.model.dto.quiz.QuizQuestionInputDTO;
import com.facem_bani_inc.daily_history_server.model.enums.ECategory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record EventDTO(
        Long id,
        ECategory category,
        TranslationDTO titleTranslations,
        TranslationDTO narrativeTranslations,
        TranslationDTO notificationTitleTranslations,
        TranslationDTO notificationBodyTranslations,
        LocalDate eventDate,
        Double impactScore,
        String sourceUrl,
        Integer pageViews30d,
        boolean isPro,
        String location,
        List<String> gallery,
        Map<String, List<QuizQuestionInputDTO>> quiz,

        // "The Long Read". `deepDive` is null on every response to a free user — the
        // full article is PRO-only. `deepDiveTeaser` always travels: chapter titles and
        // word count are the pitch. Both are JSON strings keyed by language, passed
        // through to the client untouched.
        String deepDive,
        String deepDiveTeaser
) {
}
