package com.facem_bani_inc.daily_history_server.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body for sending a friend request by username. */
public record FriendRequestDTO(
        @NotBlank(message = "Username is required")
        @Size(max = 50)
        String username
) {}
