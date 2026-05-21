package com.facem_bani_inc.daily_history_server.service;

import com.facem_bani_inc.daily_history_server.entity.*;
import com.facem_bani_inc.daily_history_server.model.dto.*;
import com.facem_bani_inc.daily_history_server.repository.EventRepository;
import com.facem_bani_inc.daily_history_server.repository.QuizRepository;
import com.facem_bani_inc.daily_history_server.repository.UserGamificationRepository;
import com.facem_bani_inc.daily_history_server.repository.UserQuizAttemptRepository;
import com.facem_bani_inc.daily_history_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.facem_bani_inc.daily_history_server.utils.Constants.*;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizService {

    private final QuizRepository quizRepository;
    private final EventRepository eventRepository;
    private final UserQuizAttemptRepository attemptRepository;
    private final UserGamificationRepository gamificationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = QUIZ_BY_EVENT_ID, key = "#eventId + '-' + #language")
    public QuizResponseDTO getQuizForEvent(Long eventId, String language) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResponseStatusException(NOT_FOUND, "Event not found: " + eventId);
        }
        Quiz quiz = quizRepository.findByEventIdAndLanguage(eventId, language)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND,
                        "Quiz not found for eventId=" + eventId + " lang=" + language));

        List<QuizQuestionResponseDTO> questions = quiz.getQuestions().stream()
                .map(q -> new QuizQuestionResponseDTO(
                        q.getQuestionKey(),
                        q.getQuestionText(),
                        q.getExplanation(),
                        q.getOptions().stream()
                                .map(o -> new QuizOptionResponseDTO(o.getOptionId(), o.getText()))
                                .toList()
                ))
                .toList();

        return new QuizResponseDTO(eventId, language, questions);
    }

    @Transactional(readOnly = true)
    public QuizStatusDTO getQuizStatus(Long eventId, Long userId) {
        return attemptRepository.findByUserIdAndEventId(userId, eventId)
                .map(a -> new QuizStatusDTO(eventId, true, a.getCorrectAnswers(), a.getTotalQuestions(), a.getXpEarned()))
                .orElse(new QuizStatusDTO(eventId, false, null, null, null));
    }

    @Transactional
    @CacheEvict(cacheNames = GAMIFICATION_BY_USER_ID, key = "#userId")
    public QuizResultDTO submitQuiz(Long eventId, Long userId, QuizSubmitDTO dto) {
        if (attemptRepository.existsByUserIdAndEventId(userId, eventId)) {
            throw new ResponseStatusException(CONFLICT, "Quiz already completed for this event");
        }

        Quiz quiz = quizRepository.findByEventIdAndLanguage(eventId, dto.language())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND,
                        "Quiz not found for eventId=" + eventId + " lang=" + dto.language()));

        Map<String, QuizQuestion> questionByKey = quiz.getQuestions().stream()
                .collect(Collectors.toMap(QuizQuestion::getQuestionKey, Function.identity()));

        List<QuizAnswerResultDTO> answerResults = new ArrayList<>();
        int correctCount = 0;

        for (QuizAnswerDTO answer : dto.answers()) {
            QuizQuestion question = questionByKey.get(answer.questionKey());
            if (question == null) continue;

            boolean isCorrect = question.getCorrectOptionId().equals(answer.selectedOptionId());
            if (isCorrect) correctCount++;

            answerResults.add(new QuizAnswerResultDTO(
                    answer.questionKey(),
                    answer.selectedOptionId(),
                    question.getCorrectOptionId(),
                    isCorrect
            ));
        }

        int totalQuestions = questionByKey.size();
        boolean perfectScore = correctCount == totalQuestions && totalQuestions > 0;
        int xpEarned = perfectScore ? XP_QUIZ_PERFECT : XP_QUIZ_PARTIAL;

        UserQuizAttempt attempt = new UserQuizAttempt();
        attempt.setUserId(userId);
        attempt.setEventId(eventId);
        attempt.setCorrectAnswers(correctCount);
        attempt.setTotalQuestions(totalQuestions);
        attempt.setXpEarned(xpEarned);
        attempt.setCompletedAt(LocalDateTime.now());

        for (QuizAnswerResultDTO result : answerResults) {
            UserQuizAnswer quizAnswer = new UserQuizAnswer();
            quizAnswer.setAttempt(attempt);
            quizAnswer.setQuestionKey(result.questionKey());
            quizAnswer.setSelectedOptionId(result.selectedOptionId());
            quizAnswer.setCorrect(result.isCorrect());
            attempt.getAnswers().add(quizAnswer);
        }

        attemptRepository.save(attempt);
        awardXp(userId, xpEarned);

        log.info("Quiz submitted: userId={} eventId={} correct={}/{} xp={}", userId, eventId, correctCount, totalQuestions, xpEarned);
        return new QuizResultDTO(correctCount, totalQuestions, xpEarned, perfectScore, answerResults);
    }

    @Transactional(readOnly = true)
    public List<UserQuizAttemptDTO> getUserAttempts(Long userId) {
        return attemptRepository.findAllByUserIdOrderByCompletedAtDesc(userId).stream()
                .map(a -> new UserQuizAttemptDTO(
                        a.getId(),
                        a.getEventId(),
                        a.getCorrectAnswers(),
                        a.getTotalQuestions(),
                        a.getXpEarned(),
                        a.getCorrectAnswers().equals(a.getTotalQuestions()) && a.getTotalQuestions() > 0,
                        a.getCompletedAt()
                ))
                .toList();
    }

    private void awardXp(Long userId, int xpToAdd) {
        UserGamification ug = gamificationRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
                    UserGamification newUg = new UserGamification();
                    newUg.setUser(user);
                    return newUg;
                });
        ug.setTotalXP((ug.getTotalXP() != null ? ug.getTotalXP() : 0) + xpToAdd);
        gamificationRepository.save(ug);
    }
}
