-- Per-language push-notification hook (TikTok-style) for each event.
-- Two optional translation rows per event: one for the notification title, one for the
-- body. Nullable because historical events (created before this feature) and refresh-mode
-- filler events won't carry a hook — the mobile app falls back to its client-side template.
ALTER TABLE events
    ADD COLUMN notification_title_translations_id BIGINT UNIQUE REFERENCES translations (id),
    ADD COLUMN notification_body_translations_id  BIGINT UNIQUE REFERENCES translations (id);
