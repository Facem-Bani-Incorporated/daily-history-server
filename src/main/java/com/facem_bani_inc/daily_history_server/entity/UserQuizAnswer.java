package com.facem_bani_inc.daily_history_server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "user_quiz_answers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserQuizAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    @ToString.Exclude
    private UserQuizAttempt attempt;

    @Column(name = "question_key", nullable = false, length = 20)
    private String questionKey;

    @Column(name = "selected_option_id", nullable = false, length = 10)
    private String selectedOptionId;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;
}
