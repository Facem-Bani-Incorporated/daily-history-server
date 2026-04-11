package com.facem_bani_inc.daily_history_server.service;

import com.facem_bani_inc.daily_history_server.entity.User;
import com.facem_bani_inc.daily_history_server.entity.UserGamification;
import com.facem_bani_inc.daily_history_server.model.dto.GamificationSyncDTO;
import com.facem_bani_inc.daily_history_server.model.dto.LeaderboardDTO;
import com.facem_bani_inc.daily_history_server.repository.UserGamificationRepository;
import com.facem_bani_inc.daily_history_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.facem_bani_inc.daily_history_server.utils.Constants.GAMIFICATION_BY_USER_ID;
import static com.facem_bani_inc.daily_history_server.utils.Constants.LEADERBOARD;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class GamificationService {

    private final UserGamificationRepository userGamificationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = LEADERBOARD, key = "'all'")
    public List<LeaderboardDTO> getAllLeaderboardData() {
        return userGamificationRepository.findAllWithUsers().stream()
                .map(ug -> new LeaderboardDTO(
                        ug.getUser().getId(),
                        ug.getUser().getUsername(),
                        ug.getTotalXP(),
                        ug.getCurrentStreak(),
                        ug.getTotalEventsRead(),
                        ug.getDailyGoalsCompleted()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = GAMIFICATION_BY_USER_ID, key = "#userId")
    public GamificationSyncDTO getGamification(Long userId) {
        UserGamification userGamification = userGamificationRepository.findByUserIdWithSavedEvents(userId).orElse(null);
        if (userGamification == null) {
            return new GamificationSyncDTO(0, 0, 0, 0, 0, null, null, List.of());
        }
        return toDto(userGamification);
    }

    @Transactional
    @CacheEvict(cacheNames = GAMIFICATION_BY_USER_ID, key = "#userId")
    public void syncGamification(Long userId, GamificationSyncDTO dto) {
        UserGamification ug = userGamificationRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
                    UserGamification newUg = new UserGamification();
                    newUg.setUser(user);
                    return newUg;
                });

        ug.setTotalXP(safeInt(dto.totalXP()));
        ug.setCurrentStreak(safeInt(dto.currentStreak()));
        ug.setLongestStreak(Math.max(safeInt(dto.longestStreak()), safeInt(dto.currentStreak())));
        ug.setTotalEventsRead(safeInt(dto.totalEventsRead()));
        ug.setDailyGoalsCompleted(safeInt(dto.dailyGoalsCompleted()));
        ug.setLastActiveDate(dto.lastActiveDate() != null ? LocalDate.parse(dto.lastActiveDate()) : null);
        ug.setGamificationData(dto.gamificationData());
        ug.getSavedEvents().clear();
        if (dto.savedEvents() != null) {
            ug.getSavedEvents().addAll(dto.savedEvents());
        }

        userGamificationRepository.save(ug);
        log.info("Gamification synced for userId: {}", userId);
    }

    private GamificationSyncDTO toDto(UserGamification ug) {
        return new GamificationSyncDTO(
                ug.getTotalXP(),
                ug.getCurrentStreak(),
                ug.getLongestStreak(),
                ug.getTotalEventsRead(),
                ug.getDailyGoalsCompleted(),
                ug.getLastActiveDate() != null ? ug.getLastActiveDate().toString() : null,
                ug.getGamificationData(),
                ug.getSavedEvents() != null ? new ArrayList<>(ug.getSavedEvents()) : List.of()
        );
    }

    private int safeInt(Integer value) {
        return value != null ? Math.max(value, 0) : 0;
    }
}
