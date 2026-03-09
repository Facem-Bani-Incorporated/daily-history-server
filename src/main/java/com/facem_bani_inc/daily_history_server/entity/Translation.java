package com.facem_bani_inc.daily_history_server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "translations")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Translation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "en", nullable = false)
    private String en;

    @Column(name = "ro", nullable = false)
    private String ro;

    @Column(name = "es", nullable = false)
    private String es;

    @Column(name = "de", nullable = false)
    private String de;

    @Column(name = "fr", nullable = false)
    private String fr;

}
