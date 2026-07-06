package com.facem_bani_inc.daily_history_server.model.dto;

/**
 * A friend or a pending friend request.
 * {@code friendshipId} identifies the underlying row (used to accept/decline a request);
 * {@code userId} identifies the other person.
 */
public record FriendDTO(
        Long friendshipId,
        Long userId,
        String username,
        String avatarUrl,
        boolean pro
) {}
