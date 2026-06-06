package com.facem_bani_inc.daily_history_server.controller;

import com.facem_bani_inc.daily_history_server.model.dto.RevenueCatWebhookPayload;
import com.facem_bani_inc.daily_history_server.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class RevenueCatWebhookController {

    private final UserService userService;

    @Value("${revenuecat.webhook-auth-header}")
    private String webhookAuthHeader;

    private static final Set<String> PRO_EVENTS = Set.of(
            "INITIAL_PURCHASE", "RENEWAL", "UNCANCELLATION", "NON_RENEWING_PURCHASE"
    );
    private static final Set<String> FREE_EVENTS = Set.of(
            "CANCELLATION", "EXPIRATION", "REFUND", "BILLING_ISSUES_DETECTED"
    );

    @PostMapping("/revenuecat")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody RevenueCatWebhookPayload payload) {

        if (!webhookAuthHeader.equals(authHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String type = payload.event().type();

        try {
            if ("TRANSFER".equals(type)) {
                applyProStatus(payload.event().transferredFrom(), false, type);
                applyProStatus(payload.event().transferredTo(), true, type);
            } else if (PRO_EVENTS.contains(type)) {
                updateOne(payload.event().appUserId(), true, type);
            } else if (FREE_EVENTS.contains(type)) {
                updateOne(payload.event().appUserId(), false, type);
            }
        } catch (Exception e) {
            log.error("Failed to process RevenueCat webhook (type={}): {}", type, e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    private void applyProStatus(List<String> appUserIds, boolean isPro, String type) {
        if (appUserIds == null) return;
        for (String id : appUserIds) {
            updateOne(id, isPro, type);
        }
    }

    private void updateOne(String appUserId, boolean isPro, String type) {
        if (appUserId == null) return;
        try {
            Long userId = Long.parseLong(appUserId);
            userService.updateProStatus(userId, isPro);
            log.info("Pro status set to {} for user {} via webhook event {}", isPro, userId, type);
        } catch (NumberFormatException e) {
            log.warn("RevenueCat webhook {} skipped non-numeric app_user_id: {}", type, appUserId);
        }
    }
}
