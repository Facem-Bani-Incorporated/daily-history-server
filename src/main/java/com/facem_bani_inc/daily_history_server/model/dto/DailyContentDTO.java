package com.facem_bani_inc.daily_history_server.model.dto;

import java.time.LocalDate;
import java.util.List;

public record DailyContentDTO(
        LocalDate dateProcessed,
        List<EventDTO> events
) {
}
