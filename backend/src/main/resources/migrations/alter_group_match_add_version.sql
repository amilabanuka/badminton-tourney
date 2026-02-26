-- Migration: Add optimistic locking version column to league_game_day_group_match

ALTER TABLE league_game_day_group_match
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

