package com.facem_bani_inc.daily_history_server.controller;

import com.facem_bani_inc.daily_history_server.model.dto.ApiResponse;
import com.facem_bani_inc.daily_history_server.model.dto.SupportMessageRequest;
import com.facem_bani_inc.daily_history_server.service.SupportMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
public class SupportMessageController {

    private final SupportMessageService supportMessageService;

    @PostMapping
    public ResponseEntity<ApiResponse> sendSupportMessage(@RequestBody SupportMessageRequest request) {
        supportMessageService.saveAndSendMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "The message was sent successfully"));
    }
}
