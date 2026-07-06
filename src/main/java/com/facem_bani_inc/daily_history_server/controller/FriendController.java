package com.facem_bani_inc.daily_history_server.controller;

import com.facem_bani_inc.daily_history_server.model.dto.FriendDTO;
import com.facem_bani_inc.daily_history_server.model.dto.FriendRequestDTO;
import com.facem_bani_inc.daily_history_server.model.dto.LeaderboardDTO;
import com.facem_bani_inc.daily_history_server.security.service.UserDetailsImpl;
import com.facem_bani_inc.daily_history_server.service.FriendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    /** Send a friend request by username (auto-accepts a matching incoming request). */
    @PostMapping("/requests")
    public ResponseEntity<FriendDTO> sendRequest(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody FriendRequestDTO request) {
        return ResponseEntity.ok(friendService.sendRequest(userDetails.getId(), request.username()));
    }

    /** Pending requests I have received. */
    @GetMapping("/requests/incoming")
    public List<FriendDTO> incoming(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return friendService.getIncomingRequests(userDetails.getId());
    }

    /** Pending requests I have sent. */
    @GetMapping("/requests/outgoing")
    public List<FriendDTO> outgoing(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return friendService.getOutgoingRequests(userDetails.getId());
    }

    @PostMapping("/requests/{friendshipId}/accept")
    public ResponseEntity<Void> accept(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long friendshipId) {
        friendService.respondToRequest(userDetails.getId(), friendshipId, true);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/requests/{friendshipId}/decline")
    public ResponseEntity<Void> decline(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long friendshipId) {
        friendService.respondToRequest(userDetails.getId(), friendshipId, false);
        return ResponseEntity.noContent().build();
    }

    /** My accepted friends. */
    @GetMapping
    public List<FriendDTO> friends(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return friendService.getFriends(userDetails.getId());
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeFriend(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long userId) {
        friendService.removeFriend(userDetails.getId(), userId);
        return ResponseEntity.noContent().build();
    }

    /** Leaderboard scoped to me + my friends. */
    @GetMapping("/leaderboard")
    public List<LeaderboardDTO> leaderboard(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return friendService.getFriendsLeaderboard(userDetails.getId());
    }
}
