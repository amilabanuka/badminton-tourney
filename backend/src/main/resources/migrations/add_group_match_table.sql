-- Migration: Add league_game_day_group_match table

CREATE TABLE IF NOT EXISTS league_game_day_group_match (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    match_order INT NOT NULL,
    team1_player1_id BIGINT NOT NULL,
    team1_player2_id BIGINT NOT NULL,
    team2_player1_id BIGINT NOT NULL,
    team2_player2_id BIGINT NOT NULL,
    team1_score INT NULL,
    team2_score INT NULL,
    FOREIGN KEY (group_id) REFERENCES league_game_day_group(id) ON DELETE CASCADE,
    FOREIGN KEY (team1_player1_id) REFERENCES league_game_day_group_player(id) ON DELETE CASCADE,
    FOREIGN KEY (team1_player2_id) REFERENCES league_game_day_group_player(id) ON DELETE CASCADE,
    FOREIGN KEY (team2_player1_id) REFERENCES league_game_day_group_player(id) ON DELETE CASCADE,
    FOREIGN KEY (team2_player2_id) REFERENCES league_game_day_group_player(id) ON DELETE CASCADE,
    INDEX idx_lgdgm_group_id (group_id)
);

