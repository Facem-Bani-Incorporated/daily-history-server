package com.facem_bani_inc.daily_history_server.controller;

import com.facem_bani_inc.daily_history_server.entity.User;
import com.facem_bani_inc.daily_history_server.model.dto.UserProfileDTO;
import com.facem_bani_inc.daily_history_server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/current")
    public UserProfileDTO getCurrentUser() {
        User user = userService.getAuthenticatedUser();
        return new UserProfileDTO(user.getId(), user.getUsername(), user.getEmail(), user.getAvatarUrl(), user.isPro());
    }

    @PatchMapping("/{id}/pro")
    public ResponseEntity<Void> updateProStatus(@PathVariable Long id, @RequestParam boolean isPro) {
        userService.updateProStatus(id, isPro);
        return ResponseEntity.noContent().build();
    }
}
