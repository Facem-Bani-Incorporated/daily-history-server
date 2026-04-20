package com.facem_bani_inc.daily_history_server.entity;

import com.facem_bani_inc.daily_history_server.model.enums.ECategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ECategory category;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "title_translations_id", nullable = false, unique = true)
    private Translation titleTranslations;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "narrative_translations_id", nullable = false, unique = true)
    private Translation narrativeTranslations;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "is_pro", nullable = false)
    private boolean pro = false;

    @Column(name = "location")
    private String location;

    @Column(name = "impact_score", nullable = false)
    private Double impactScore;

    @Column(name = "source_url", nullable = false)
    private String sourceUrl;

    @Column(name = "page_views_30d")
    private Integer pageViews30d;

    @ElementCollection
    @CollectionTable(name = "event_gallery", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "image_url", nullable = false)
    private List<String> gallery = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_content_id", nullable = false)
    private DailyContent dailyContent;
}
