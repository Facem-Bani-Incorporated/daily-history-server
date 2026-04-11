package com.facem_bani_inc.daily_history_server.repository;

import com.facem_bani_inc.daily_history_server.entity.UserGamification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserGamificationRepository extends JpaRepository<UserGamification, Long> {

    @Query("SELECT ug FROM UserGamification ug JOIN FETCH ug.user")
    List<UserGamification> findAllWithUsers();

    @Query("SELECT ug FROM UserGamification ug LEFT JOIN FETCH ug.savedEvents WHERE ug.user.id = :userId")
    Optional<UserGamification> findByUserIdWithSavedEvents(@Param("userId") Long userId);

    Optional<UserGamification> findByUserId(Long userId);
}
