-- Migration: Add rank_score_history table

CREATE TABLE IF NOT EXISTS rank_score_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_player_id BIGINT NOT NULL,
    match_id BIGINT NOT NULL,
    previous_score DECIMAL(10, 2) NOT NULL,
    new_score DECIMAL(10, 2) NOT NULL,
    changed_at BIGINT NOT NULL,
    FOREIGN KEY (tournament_player_id) REFERENCES tournament_players(id) ON DELETE CASCADE,
    FOREIGN KEY (match_id) REFERENCES league_game_day_group_match(id) ON DELETE CASCADE,
    INDEX idx_rsh_tournament_player_id (tournament_player_id),
    INDEX idx_rsh_match_id (match_id)
);

