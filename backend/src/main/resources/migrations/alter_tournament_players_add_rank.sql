ALTER TABLE tournament_players
    ADD COLUMN `rank` INT NULL,
    ADD COLUMN rank_score DECIMAL(10, 2) NOT NULL DEFAULT 0.00;


