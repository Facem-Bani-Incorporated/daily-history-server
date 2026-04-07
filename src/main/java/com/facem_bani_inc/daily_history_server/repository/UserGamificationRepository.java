package com.facem_bani_inc.daily_history_server.repository;

import com.facem_bani_inc.daily_history_server.entity.UserGamification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserGamificationRepository extends JpaRepository<UserGamification, Long> {

    Optional<UserGamification> findByUserId(Long userId);
}
