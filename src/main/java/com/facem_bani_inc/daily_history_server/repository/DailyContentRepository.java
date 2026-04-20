package com.facem_bani_inc.daily_history_server.repository;

import com.facem_bani_inc.daily_history_server.entity.DailyContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyContentRepository extends JpaRepository<DailyContent, Long> {

    Optional<DailyContent> findByDateProcessed(LocalDate dateProcessed);

    @Query("""
           SELECT dc
           FROM DailyContent dc
           LEFT JOIN FETCH dc.events e
           LEFT JOIN FETCH e.titleTranslations
           LEFT JOIN FETCH e.narrativeTranslations
           WHERE dc.dateProcessed = :date AND e.pro = false
           """)
    Optional<DailyContent> findByDateProcessedWithEvents(LocalDate date);

    @Query("""
           SELECT dc
           FROM DailyContent dc
           LEFT JOIN FETCH dc.events e
           LEFT JOIN FETCH e.titleTranslations
           LEFT JOIN FETCH e.narrativeTranslations
           WHERE dc.dateProcessed = :date AND e.pro = true
           """)
    Optional<DailyContent> findByDateProcessedWithProEvents(LocalDate date);
}
