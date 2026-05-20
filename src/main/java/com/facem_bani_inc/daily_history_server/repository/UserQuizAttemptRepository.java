package com.facem_bani_inc.daily_history_server.repository;

import com.facem_bani_inc.daily_history_server.entity.UserQuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserQuizAttemptRepository extends JpaRepository<UserQuizAttempt, Long> {

    Optional<UserQuizAttempt> findByUserIdAndEventId(Long userId, Long eventId);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    @Query("SELECT a FROM UserQuizAttempt a WHERE a.userId = :userId ORDER BY a.completedAt DESC")
    List<UserQuizAttempt> findAllByUserIdOrderByCompletedAtDesc(@Param("userId") Long userId);
}
