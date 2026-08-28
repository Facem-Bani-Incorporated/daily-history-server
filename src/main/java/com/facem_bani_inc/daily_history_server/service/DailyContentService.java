package com.facem_bani_inc.daily_history_server.service;

import com.facem_bani_inc.daily_history_server.entity.*;
import com.facem_bani_inc.daily_history_server.model.dto.*;
import com.facem_bani_inc.daily_history_server.model.dto.quiz.QuizOptionInputDTO;
import com.facem_bani_inc.daily_history_server.model.dto.quiz.QuizQuestionInputDTO;
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
            @CacheEvict(cacheNames = GUEST_CONTENT_DATES, allEntries = true),
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

    /**
     * The whole day — free and PRO events — with the long read included.
     * <p>
     * Callers MUST have verified the requester holds an active PRO entitlement before
     * calling this. It has its own cache region for that reason: {@code DAILY_CONTENT_BY_DATE}
     * is keyed on date alone, so populating it from a subscriber's request would serve
     * the full article to every free user asking for the same day.
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = FULL_DAILY_CONTENT_BY_DATE, key = "#date")
    public DailyContentDTO getFullDailyContentByDate(LocalDate date) {
        DailyContent dailyContent = dailyContentRepository.findByDateProcessedWithEvents(date)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "DailyContent not found for date: " + date));

        return dailyContentToDto(dailyContent, true);
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
        List<Event> events = dailyContentRepository.findFreeEventsByDate(date);
        if (events.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "No content available for date: " + date);
        }
        return events.stream().map(this::toEventDto).toList();
    }

    /**
     * Dates with guest-visible content, newest first. Consumed by the public
     * website to enumerate the archive it can build. Future dates are excluded
     * so scheduled-but-unpublished content never leaks.
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = GUEST_CONTENT_DATES)
    public List<LocalDate> getGuestContentDates() {
        return dailyContentRepository.findGuestContentDates(LocalDate.now());
    }

    private void populateDailyContentFromDto(DailyContent dailyContent, DailyContentDTO dailyContentDTO) {
        if (dailyContentDTO.events() == null || dailyContentDTO.events().isEmpty()) return;

        for (EventDTO eventDTO : dailyContentDTO.events()) {
            Event event = new Event();
            event.setCategory(eventDTO.category());
            event.setTitleTranslations(toTranslationEntity(eventDTO.titleTranslations()));
            event.setNarrativeTranslations(toTranslationEntity(eventDTO.narrativeTranslations()));
            event.setNotificationTitleTranslations(toNotificationTranslationEntity(eventDTO.notificationTitleTranslations()));
            event.setNotificationBodyTranslations(toNotificationTranslationEntity(eventDTO.notificationBodyTranslations()));
            event.setEventDate(eventDTO.eventDate());
            event.setImpactScore(eventDTO.impactScore());
            event.setSourceUrl(eventDTO.sourceUrl());
            event.setPageViews30d(eventDTO.pageViews30d());
            event.setPro(eventDTO.isPro());
            event.setLocation(eventDTO.location());
            // The pipeline sends the long read back on refresh-mode filler events, so a
            // null here means "not generated", never "clear what's stored".
            event.setDeepDive(eventDTO.deepDive());
            event.setDeepDiveTeaser(eventDTO.deepDiveTeaser());
            event.setParallelUniverse(eventDTO.parallelUniverse());
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

    /**
     * Notification translations are optional. Skip creating a row when the DTO is null
     * or every language is blank — the column stays null and the app falls back to its
     * own client-side notification hook, avoiding junk empty translation rows.
     */
    private Translation toNotificationTranslationEntity(TranslationDTO dto) {
        if (dto == null) return null;
        boolean allBlank = isBlank(dto.en()) && isBlank(dto.ro()) && isBlank(dto.es())
                && isBlank(dto.de()) && isBlank(dto.fr());
        if (allBlank) return null;
        Translation translation = new Translation();
        translation.setEn(nullToEmpty(dto.en()));
        translation.setRo(nullToEmpty(dto.ro()));
        translation.setEs(nullToEmpty(dto.es()));
        translation.setDe(nullToEmpty(dto.de()));
        translation.setFr(nullToEmpty(dto.fr()));
        return translation;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private DailyContentDTO dailyContentToDto(DailyContent dailyContent) {
        return dailyContentToDto(dailyContent, false);
    }

    private DailyContentDTO dailyContentToDto(DailyContent dailyContent, boolean includeDeepDive) {
        List<EventDTO> eventDtos = new ArrayList<>();
        if (dailyContent.getEvents() != null) {
            for (Event event : dailyContent.getEvents()) {
                eventDtos.add(toEventDto(event, includeDeepDive));
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

    private TranslationDTO toTranslationDtoOrNull(Translation translation) {
        return translation == null ? null : toTranslationDto(translation);
    }

    /**
     * Map an event for a response that must NOT carry the long read.
     * Every caller that serves free or unauthenticated users lands here.
     */
    private EventDTO toEventDto(Event event) {
        return toEventDto(event, false);
    }

    /**
     * @param includeDeepDive true only when the caller has already established the
     *                        requester holds an active PRO entitlement. The teaser
     *                        travels either way — it is the pitch, not the content.
     */
    private EventDTO toEventDto(Event event, boolean includeDeepDive) {
        return new EventDTO(
                event.getId(),
                event.getCategory(),
                toTranslationDto(event.getTitleTranslations()),
                toTranslationDto(event.getNarrativeTranslations()),
                toTranslationDtoOrNull(event.getNotificationTitleTranslations()),
                toTranslationDtoOrNull(event.getNotificationBodyTranslations()),
                event.getEventDate(),
                event.getImpactScore(),
                event.getSourceUrl(),
                event.getPageViews30d(),
                event.isPro(),
                event.getLocation(),
                event.getGallery() != null ? new ArrayList<>(event.getGallery()) : new ArrayList<>(),
                null,
                includeDeepDive ? event.getDeepDive() : null,
                event.getDeepDiveTeaser(),
                event.getParallelUniverse()
        );
    }
}
