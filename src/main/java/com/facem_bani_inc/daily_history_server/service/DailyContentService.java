package com.facem_bani_inc.daily_history_server.service;

import com.facem_bani_inc.daily_history_server.entity.*;
import com.facem_bani_inc.daily_history_server.model.dto.*;
import com.facem_bani_inc.daily_history_server.repository.DailyContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.facem_bani_inc.daily_history_server.utils.Constants.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyContentService {

    private final DailyContentRepository dailyContentRepository;

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = DAILY_CONTENT_BY_DATE, key = "#dailyContentDTO.dateProcessed()"),
            @CacheEvict(cacheNames = PRO_DAILY_CONTENT_BY_DATE, key = "#dailyContentDTO.dateProcessed()"),
            @CacheEvict(cacheNames = GUEST_TOP_EVENT, key = "#dailyContentDTO.dateProcessed()"),
            @CacheEvict(cacheNames = QUIZ_BY_EVENT_ID, allEntries = true)
    })
    public DailyContent upsertDailyContent(DailyContentDTO dailyContentDTO) {
        LocalDate date = dailyContentDTO.dateProcessed();
        DailyContent dailyContent = dailyContentRepository.findByDateProcessed(date)
                .orElseGet(DailyContent::new);
        dailyContent.setDateProcessed(date);
        dailyContent.getEvents().clear();
        populateDailyContentFromDto(dailyContent, dailyContentDTO);
        DailyContent savedContent = dailyContentRepository.save(dailyContent);
        log.info("DailyContent upsert success for dateProcessed: {} and dailyContentId: {}", date, savedContent.getId());

        return savedContent;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = DAILY_CONTENT_BY_DATE, key = "#date")
    public DailyContentDTO getDailyContentByDate(LocalDate date) {
        DailyContent dailyContent = dailyContentRepository.findByDateProcessedWithEvents(date)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "DailyContent not found for date: " + date));

        return dailyContentToDto(dailyContent);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = PRO_DAILY_CONTENT_BY_DATE, key = "#date")
    public DailyContentDTO getProDailyContentByDate(LocalDate date) {
        DailyContent dailyContent = dailyContentRepository.findByDateProcessedWithProEvents(date)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "DailyContent not found for date: " + date));

        return dailyContentToDto(dailyContent);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = GUEST_TOP_EVENT, key = "#date")
    public List<EventDTO> getGuestTopEvents(LocalDate date) {
        List<Event> events = dailyContentRepository.findTopTwoFreeEventsByDate(date);
        if (events.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "No content available for date: " + date);
        }
        return events.stream().map(this::toEventDto).toList();
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
            event.setPro(eventDTO.isPro());
            event.setLocation(eventDTO.location());
            event.setGallery(eventDTO.gallery() != null ? new ArrayList<>(eventDTO.gallery()) : new ArrayList<>());
            event.setDailyContent(dailyContent);

            if (eventDTO.quiz() != null && !eventDTO.quiz().isEmpty()) {
                event.setQuiz(buildQuiz(event, eventDTO.quiz()));
            }

            dailyContent.getEvents().add(event);
        }
    }

    private Quiz buildQuiz(Event event, Map<String, List<QuizQuestionInputDTO>> quizData) {
        Quiz quiz = new Quiz();
        quiz.setEvent(event);

        for (Map.Entry<String, List<QuizQuestionInputDTO>> entry : quizData.entrySet()) {
            String language = entry.getKey();
            for (QuizQuestionInputDTO qDto : entry.getValue()) {
                QuizQuestion question = new QuizQuestion();
                question.setQuiz(quiz);
                question.setQuestionKey(qDto.id());
                question.setLanguage(language);
                question.setQuestionText(qDto.question());
                question.setExplanation(qDto.explanation());
                question.setCorrectOptionId(qDto.correctId());

                for (QuizOptionInputDTO oDto : qDto.options()) {
                    QuizOption option = new QuizOption();
                    option.setQuestion(question);
                    option.setOptionId(oDto.id());
                    option.setText(oDto.text());
                    question.getOptions().add(option);
                }

                quiz.getQuestions().add(question);
            }
        }

        return quiz;
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
                eventDtos.add(toEventDto(event));
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

    private EventDTO toEventDto(Event event) {
        return new EventDTO(
                event.getId(),
                event.getCategory(),
                toTranslationDto(event.getTitleTranslations()),
                toTranslationDto(event.getNarrativeTranslations()),
                event.getEventDate(),
                event.getImpactScore(),
                event.getSourceUrl(),
                event.getPageViews30d(),
                event.isPro(),
                event.getLocation(),
                event.getGallery() != null ? new ArrayList<>(event.getGallery()) : new ArrayList<>(),
                null
        );
    }
}
