package com.facem_bani_inc.daily_history_server.controller;

import com.facem_bani_inc.daily_history_server.model.dto.RevenueCatWebhookPayload;
import com.facem_bani_inc.daily_history_server.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            "INITIAL_PURCHASE", "RENEWAL", "UNCANCELLATION"
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
        String appUserId = payload.event().appUserId();

        try {
            Long userId = Long.parseLong(appUserId);
            if (PRO_EVENTS.contains(type)) {
                userService.updateProStatus(userId, true);
                log.info("Pro status activated for user {} via webhook event {}", userId, type);
            } else if (FREE_EVENTS.contains(type)) {
                userService.updateProStatus(userId, false);
                log.info("Pro status deactivated for user {} via webhook event {}", userId, type);
            }
        } catch (NumberFormatException e) {
            log.warn("RevenueCat webhook received non-numeric app_user_id: {}", appUserId);
        } catch (Exception e) {
            log.error("Failed to process RevenueCat webhook for user {}: {}", appUserId, e.getMessage());
        }
        return ResponseEntity.ok().build();
    }
}
