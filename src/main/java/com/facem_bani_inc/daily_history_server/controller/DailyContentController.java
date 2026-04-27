package com.facem_bani_inc.daily_history_server.controller;

import com.facem_bani_inc.daily_history_server.entity.DailyContent;
import com.facem_bani_inc.daily_history_server.model.dto.DailyContentDTO;
import com.facem_bani_inc.daily_history_server.security.service.UserDetailsImpl;
import com.facem_bani_inc.daily_history_server.service.DailyContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/daily-content")
@RequiredArgsConstructor
public class DailyContentController {

    private final DailyContentService dailyContentService;

    @PostMapping
    public ResponseEntity<Long> addDailyContent(@RequestBody DailyContentDTO dailyContentDTO) {
        DailyContent savedContent = dailyContentService.upsertDailyContent(dailyContentDTO);
        return ResponseEntity.ok(savedContent.getId());
    }

    @GetMapping(value = "/by-date")
    public DailyContentDTO getDailyContent(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return dailyContentService.getDailyContentByDate(date);
    }

    @GetMapping(value = "/pro/by-date")
    public DailyContentDTO getProDailyContent(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (!userDetails.isPro()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pro subscription required");
        }
        return dailyContentService.getProDailyContentByDate(date);
    }
}
