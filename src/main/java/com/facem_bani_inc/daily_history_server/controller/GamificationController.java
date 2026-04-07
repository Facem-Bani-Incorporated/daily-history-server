package com.facem_bani_inc.daily_history_server.controller;

import com.facem_bani_inc.daily_history_server.model.dto.ApiResponse;
import com.facem_bani_inc.daily_history_server.model.dto.GamificationSyncDTO;
import com.facem_bani_inc.daily_history_server.security.service.UserDetailsImpl;
import com.facem_bani_inc.daily_history_server.service.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gamification")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService gamificationService;

    @GetMapping
    public ResponseEntity<GamificationSyncDTO> getGamification(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        GamificationSyncDTO dto = gamificationService.getGamification(userDetails.getId());
        return ResponseEntity.ok(dto);
    }

    @PutMapping
    public ResponseEntity<ApiResponse> syncGamification(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody GamificationSyncDTO dto) {
        gamificationService.syncGamification(userDetails.getId(), dto);
        return ResponseEntity.ok(new ApiResponse(true, "Gamification synced successfully"));
    }
}
