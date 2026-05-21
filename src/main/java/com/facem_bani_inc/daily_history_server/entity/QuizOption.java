package com.facem_bani_inc.daily_history_server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "quiz_options")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @ToString.Exclude
    private QuizQuestion question;

    @Column(name = "option_id", nullable = false, length = 10)
    private String optionId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;
}
