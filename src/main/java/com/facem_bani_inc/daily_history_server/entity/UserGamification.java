package com.facem_bani_inc.daily_history_server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_gamification")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserGamification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Integer totalXP = 0;

    @Column(nullable = false)
    private Integer currentStreak = 0;

    @Column(nullable = false)
    private Integer longestStreak = 0;

    @Column(nullable = false)
    private Integer totalEventsRead = 0;

    @Column(nullable = false)
    private Integer dailyGoalsCompleted = 0;

    private LocalDate lastActiveDate;

    @Column(columnDefinition = "text")
    private String gamificationData;

    @ElementCollection
    @CollectionTable(name = "user_saved_events", joinColumns = @JoinColumn(name = "user_gamification_id"))
    @Column(name = "event_id", nullable = false)
    private List<Long> savedEvents = new ArrayList<>();
}