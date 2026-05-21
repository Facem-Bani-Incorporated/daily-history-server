package com.facem_bani_inc.daily_history_server.repository;

import com.facem_bani_inc.daily_history_server.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.questions qn WHERE q.event.id = :eventId AND qn.language = :language")
    Optional<Quiz> findByEventIdAndLanguage(@Param("eventId") Long eventId, @Param("language") String language);

    boolean existsByEventId(Long eventId);
}
