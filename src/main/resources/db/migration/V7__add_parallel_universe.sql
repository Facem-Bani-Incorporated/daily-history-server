-- "Parallel Universes" — a branching what-if game per event.
--
-- One JSON text column keyed by language, same shape and reasoning as V6's deep_dive:
-- the database never queries inside it, so text keeps the mapping trivial.
--
-- Unlike the long read this is NOT split into a public and a gated column. The tree is
-- worthless without the UI that runs it, and there is no natural line to draw inside
-- the payload — the client decides who may play.
--
-- Nullable, and expected to be null on most rows: only the highest-impact event of each
-- tier gets a game, because the tree costs about a long read to generate.
ALTER TABLE events
    ADD COLUMN parallel_universe TEXT;
