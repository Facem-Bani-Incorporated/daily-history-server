package com.facem_bani_inc.daily_history_server.service;

import com.facem_bani_inc.daily_history_server.entity.DailyContent;
import com.facem_bani_inc.daily_history_server.entity.Event;
import com.facem_bani_inc.daily_history_server.entity.Translation;
import com.facem_bani_inc.daily_history_server.model.dto.DailyContentDTO;
import com.facem_bani_inc.daily_history_server.model.dto.EventDTO;
import com.facem_bani_inc.daily_history_server.model.dto.TranslationDTO;
import com.facem_bani_inc.daily_history_server.repository.DailyContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class DailyContentService {

    private final DailyContentRepository dailyContentRepository;

    @Transactional
    public DailyContent upsertDailyContent(DailyContentDTO dailyContentDTO) {
        LocalDate date = dailyContentDTO.dateProcessed();
        DailyContent dailyContent = dailyContentRepository.findByDateProcessed(date)
                .orElseGet(DailyContent::new);
        dailyContent.setDateProcessed(date);
        dailyContent.getEvents().clear();
        populateDailyContentFromDto(dailyContent, dailyContentDTO);

        return dailyContentRepository.save(dailyContent);
    }

    @Transactional(readOnly = true)
    public DailyContentDTO getDailyContentByDate(LocalDate date) {
        DailyContent dailyContent = dailyContentRepository.findByDateProcessedWithEvents(date)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "DailyContent not found for date: " + date));

        return dailyContentToDto(dailyContent);
    }

    private void populateDailyContentFromDto(DailyContent dailyContent, DailyContentDTO dailyContentDTO) {
        if (dailyContentDTO.events() == null || dailyContentDTO.events().isEmpty()) return;

        for (EventDTO eventDTO : dailyContentDTO.events()) {
            Event event = new Event();
            event.setCategory(eventDTO.category());
            event.setTitleTranslations(toTranslationEntity(eventDTO.titleTranslations()));
            event.setNarrativeTranslations(toTranslationEntity(eventDTO.narrativeTranslations()));
            event.setEventDate(eventDTO.eventDate());
            event.setImpactScore(eventDTO.impactScore());
            event.setSourceUrl(eventDTO.sourceUrl());
            event.setPageViews30d(eventDTO.pageViews30d());
            event.setGallery(eventDTO.gallery() != null ? new ArrayList<>(eventDTO.gallery()) : new ArrayList<>());
            event.setDailyContent(dailyContent);
            dailyContent.getEvents().add(event);
        }
    }

    private Translation toTranslationEntity(TranslationDTO dto) {
        Translation translation = new Translation();
        translation.setEn(dto.en());
        translation.setRo(dto.ro());
        translation.setEs(dto.es());
        translation.setDe(dto.de());
        translation.setFr(dto.fr());
        return translation;
    }

    private DailyContentDTO dailyContentToDto(DailyContent dailyContent) {
        List<EventDTO> eventDtos = new ArrayList<>();
        if (dailyContent.getEvents() != null) {
            for (Event event : dailyContent.getEvents()) {
                eventDtos.add(new EventDTO(
                        event.getCategory(),
                        toTranslationDto(event.getTitleTranslations()),
                        toTranslationDto(event.getNarrativeTranslations()),
                        event.getEventDate(),
                        event.getImpactScore(),
                        event.getSourceUrl(),
                        event.getPageViews30d(),
                        event.getGallery() != null ? new ArrayList<>(event.getGallery()) : new ArrayList<>()
                ));
            }
        }
        return new DailyContentDTO(dailyContent.getDateProcessed(), eventDtos);
    }

    private TranslationDTO toTranslationDto(Translation translation) {
        return new TranslationDTO(
                translation.getEn(),
                translation.getRo(),
                translation.getEs(),
                translation.getDe(),
                translation.getFr()
        );
    }
}
