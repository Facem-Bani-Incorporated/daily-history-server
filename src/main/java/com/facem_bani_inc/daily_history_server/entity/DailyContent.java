package com.facem_bani_inc.daily_history_server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "daily_content")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_processed")
    private LocalDate dateProcessed;

    @OneToMany(mappedBy = "dailyContent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = LAZY)
    private List<Event> events = new ArrayList<>();
}
