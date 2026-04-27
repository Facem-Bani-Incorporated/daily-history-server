package com.facem_bani_inc.daily_history_server.service;

import com.facem_bani_inc.daily_history_server.entity.User;
import com.facem_bani_inc.daily_history_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RevenueCatService revenueCatService;

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
}
