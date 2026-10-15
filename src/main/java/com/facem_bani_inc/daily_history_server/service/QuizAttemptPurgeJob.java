package com.facem_bani_inc.daily_history_server.service;

import com.facem_bani_inc.daily_history_server.repository.UserQuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Lazy(false)
@RequiredArgsConstructor
@Slf4j
public class QuizAttemptPurgeJob {

    private final UserQuizAttemptRepository attemptRepository;

    @Scheduled(cron = "${quiz-attempt-purge-job.cron}")
    @Transactional
    public void purgeAttemptsOlderThanOneMonth() {
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(1);
        int attempts = attemptRepository.deleteAttemptsCompletedBefore(cutoff);
        log.info("Quiz attempt purge: deleted {} attempts completed before {}", attempts, cutoff.toLocalDate());
    }
}
