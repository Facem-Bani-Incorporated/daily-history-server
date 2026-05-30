package com.facem_bani_inc.daily_history_server.service;

import com.facem_bani_inc.daily_history_server.entity.User;
import com.facem_bani_inc.daily_history_server.model.enums.EAuthProvider;
import com.facem_bani_inc.daily_history_server.repository.SupportMessageRepository;
import com.facem_bani_inc.daily_history_server.repository.UserGamificationRepository;
import com.facem_bani_inc.daily_history_server.repository.UserQuizAttemptRepository;
import com.facem_bani_inc.daily_history_server.repository.UserRepository;
import com.facem_bani_inc.daily_history_server.security.service.AvatarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserGamificationRepository userGamificationRepository;
    private final UserQuizAttemptRepository userQuizAttemptRepository;
    private final SupportMessageRepository supportMessageRepository;
    private final RevenueCatService revenueCatService;
    private final AvatarService avatarService;

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public void updateProStatus(Long userId, boolean isPro) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found: " + userId));
        user.setPro(isPro);
        userRepository.save(user);
    }

    @Transactional
    public void syncProStatusFromRevenueCat(Long userId) {
        updateProStatus(userId, revenueCatService.isUserPro(userId));
    }

    @Transactional
    public void deleteAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found: " + userId));
        EAuthProvider provider = user.getAuthProvider();
        String providerUserId = user.getProviderUserId();
        userQuizAttemptRepository.deleteAnswersByUserId(userId);
        userQuizAttemptRepository.deleteAttemptsByUserId(userId);
        userGamificationRepository.findByUserId(userId)
                .ifPresent(userGamificationRepository::delete);
        supportMessageRepository.deleteAllByUserId(userId);
        userRepository.delete(user);
        if (provider == EAuthProvider.GOOGLE && providerUserId != null) {
            avatarService.deleteAvatar(provider, providerUserId);
        }
        log.info("Account deleted successfully for userId: {} provider: {}", userId, provider);
    }
}
