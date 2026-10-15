ALTER TABLE events
    ADD COLUMN notification_title_translations_id BIGINT UNIQUE REFERENCES translations (id),
    ADD COLUMN notification_body_translations_id  BIGINT UNIQUE REFERENCES translations (id),
    ADD COLUMN deep_dive                          TEXT,
    ADD COLUMN deep_dive_teaser                   TEXT,
    ADD COLUMN parallel_universe                  TEXT;

CREATE INDEX idx_events_missing_deep_dive
    ON events (event_date, impact_score DESC)
    WHERE deep_dive IS NULL;
