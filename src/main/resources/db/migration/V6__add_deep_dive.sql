-- "The Long Read" — the long-form PRO narrative for each event.
--
-- Stored as two JSON text columns rather than translation rows. The translations table
-- holds one string per language; this is one structured object per language (chapters,
-- timeline, misconception, aftermath, sources), which that shape cannot carry. The
-- database never queries inside these blobs, so text keeps the mapping trivial and
-- avoids jsonb typing concerns in Hibernate.
--
-- deep_dive        — full article. Served ONLY to authenticated PRO users.
-- deep_dive_teaser — opening words, chapter titles, word count. Served to everyone;
--                    it is the paywall pitch, so it is deliberately not secret.
--
-- Both nullable: every event published before this feature has neither, and the app
-- shows no teaser at all in that case rather than promising chapters that don't exist.
ALTER TABLE events
    ADD COLUMN deep_dive        TEXT,
    ADD COLUMN deep_dive_teaser TEXT;

-- The backfill job scans for events still missing a long read, ordered by impact.
-- Partial index: once the archive is filled in, this costs almost nothing.
CREATE INDEX idx_events_missing_deep_dive
    ON events (event_date, impact_score DESC)
    WHERE deep_dive IS NULL;
