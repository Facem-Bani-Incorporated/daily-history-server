package com.facem_bani_inc.daily_history_server.controller;

import com.facem_bani_inc.daily_history_server.model.dto.*;
import com.facem_bani_inc.daily_history_server.security.service.UserDetailsImpl;
import com.facem_bani_inc.daily_history_server.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/event/{eventId}")
    public ResponseEntity<QuizResponseDTO> getQuiz(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "en") String lang) {
        return ResponseEntity.ok(quizService.getQuizForEvent(eventId, lang));
    }

    @GetMapping("/event/{eventId}/status")
    public ResponseEntity<QuizStatusDTO> getQuizStatus(
            @PathVariable Long eventId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(quizService.getQuizStatus(eventId, userDetails.getId()));
    }

    @PostMapping("/event/{eventId}/submit")
    public ResponseEntity<QuizResultDTO> submitQuiz(
            @PathVariable Long eventId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody QuizSubmitDTO dto) {
        return ResponseEntity.ok(quizService.submitQuiz(eventId, userDetails.getId(), dto));
    }

    @GetMapping("/user/attempts")
    public ResponseEntity<List<UserQuizAttemptDTO>> getUserAttempts(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(quizService.getUserAttempts(userDetails.getId()));
    }
}
