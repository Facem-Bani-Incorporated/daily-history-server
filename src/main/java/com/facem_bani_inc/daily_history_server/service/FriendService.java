package com.facem_bani_inc.daily_history_server.service;

import com.facem_bani_inc.daily_history_server.entity.Friendship;
import com.facem_bani_inc.daily_history_server.entity.User;
import com.facem_bani_inc.daily_history_server.entity.UserGamification;
import com.facem_bani_inc.daily_history_server.model.dto.FriendDTO;
import com.facem_bani_inc.daily_history_server.model.dto.LeaderboardDTO;
import com.facem_bani_inc.daily_history_server.repository.FriendshipRepository;
import com.facem_bani_inc.daily_history_server.repository.UserGamificationRepository;
import com.facem_bani_inc.daily_history_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.facem_bani_inc.daily_history_server.model.enums.EFriendshipStatus.ACCEPTED;
import static com.facem_bani_inc.daily_history_server.model.enums.EFriendshipStatus.PENDING;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final UserGamificationRepository userGamificationRepository;

    /**
     * Send a friend request to a user identified by username. If that user has already
     * sent a pending request to me, this accepts it instead (mutual add).
     */
    @Transactional
    public FriendDTO sendRequest(Long meId, String username) {
        User target = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        if (target.getId().equals(meId)) {
            throw new ResponseStatusException(BAD_REQUEST, "You cannot add yourself");
        }

        Friendship existing = friendshipRepository.findBetween(meId, target.getId()).orElse(null);
        if (existing != null) {
            if (existing.getStatus() == ACCEPTED) {
                throw new ResponseStatusException(CONFLICT, "You are already friends");
            }
            // PENDING: if they already asked me, accept it; otherwise it's a duplicate of mine.
            if (existing.getAddressee().getId().equals(meId)) {
                existing.setStatus(ACCEPTED);
                existing.setRespondedAt(LocalDateTime.now());
                friendshipRepository.save(existing);
                log.info("Friend request auto-accepted between {} and {}", meId, target.getId());
                return toFriendDTO(existing, meId);
            }
            throw new ResponseStatusException(CONFLICT, "Friend request already sent");
        }

        User me = userRepository.findById(meId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        Friendship friendship = new Friendship();
        friendship.setRequester(me);
        friendship.setAddressee(target);
        friendship.setStatus(PENDING);
        friendship.setCreatedAt(LocalDateTime.now());
        friendshipRepository.save(friendship);
        log.info("Friend request sent from {} to {}", meId, target.getId());
        return toFriendDTO(friendship, meId);
    }

    @Transactional(readOnly = true)
    public List<FriendDTO> getIncomingRequests(Long meId) {
        return friendshipRepository.findIncoming(meId, PENDING).stream()
                .map(f -> toFriendDTO(f, meId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FriendDTO> getOutgoingRequests(Long meId) {
        return friendshipRepository.findOutgoing(meId, PENDING).stream()
                .map(f -> toFriendDTO(f, meId))
                .toList();
    }

    @Transactional
    public void respondToRequest(Long meId, Long friendshipId, boolean accept) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Request not found"));

        if (!friendship.getAddressee().getId().equals(meId)) {
            throw new ResponseStatusException(FORBIDDEN, "Only the recipient can respond to this request");
        }
        if (friendship.getStatus() != PENDING) {
            throw new ResponseStatusException(CONFLICT, "Request already handled");
        }

        if (accept) {
            friendship.setStatus(ACCEPTED);
            friendship.setRespondedAt(LocalDateTime.now());
            friendshipRepository.save(friendship);
            log.info("Friend request {} accepted by {}", friendshipId, meId);
        } else {
            friendshipRepository.delete(friendship);
            log.info("Friend request {} declined by {}", friendshipId, meId);
        }
    }

    @Transactional(readOnly = true)
    public List<FriendDTO> getFriends(Long meId) {
        return friendshipRepository.findAcceptedForUser(meId, ACCEPTED).stream()
                .map(f -> toFriendDTO(f, meId))
                .toList();
    }

    @Transactional
    public void removeFriend(Long meId, Long otherUserId) {
        Friendship friendship = friendshipRepository.findBetween(meId, otherUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Friendship not found"));
        friendshipRepository.delete(friendship);
        log.info("Friendship removed between {} and {}", meId, otherUserId);
    }

    /** Leaderboard scoped to the user and their accepted friends, ranked by total XP. */
    @Transactional(readOnly = true)
    public List<LeaderboardDTO> getFriendsLeaderboard(Long meId) {
        // Collect self + every accepted friend, keyed by id (deduplicated).
        Map<Long, User> participants = new LinkedHashMap<>();
        userRepository.findById(meId).ifPresent(u -> participants.put(u.getId(), u));
        for (Friendship f : friendshipRepository.findAcceptedForUser(meId, ACCEPTED)) {
            User other = f.getRequester().getId().equals(meId) ? f.getAddressee() : f.getRequester();
            participants.put(other.getId(), other);
        }
        if (participants.isEmpty()) {
            return List.of();
        }

        Map<Long, UserGamification> statsByUser = new LinkedHashMap<>();
        for (UserGamification ug : userGamificationRepository.findByUserIdInWithUsers(participants.keySet())) {
            statsByUser.put(ug.getUser().getId(), ug);
        }

        List<LeaderboardDTO> board = new ArrayList<>(participants.size());
        for (User user : participants.values()) {
            UserGamification ug = statsByUser.get(user.getId());
            board.add(new LeaderboardDTO(
                    user.getId(),
                    user.getUsername(),
                    ug != null ? ug.getTotalXP() : 0,
                    ug != null ? ug.getCurrentStreak() : 0,
                    ug != null ? ug.getTotalEventsRead() : 0,
                    ug != null ? ug.getDailyGoalsCompleted() : 0,
                    ug != null && ug.getMonthlyXP() != null ? ug.getMonthlyXP() : 0
            ));
        }
        board.sort(Comparator.comparingInt(LeaderboardDTO::totalXP).reversed());
        return board;
    }

    /** Maps a friendship to the "other" person relative to {@code meId}. */
    private FriendDTO toFriendDTO(Friendship f, Long meId) {
        User other = f.getRequester().getId().equals(meId) ? f.getAddressee() : f.getRequester();
        return new FriendDTO(f.getId(), other.getId(), other.getUsername(), other.getAvatarUrl(), other.isPro());
    }
}
