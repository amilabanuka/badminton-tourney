-- Migration: Add league game day tables

CREATE TABLE IF NOT EXISTS league_game_day (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    game_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    UNIQUE KEY uk_tournament_date (tournament_id, game_date),
    FOREIGN KEY (tournament_id) REFERENCES tournament(id) ON DELETE CASCADE,
    INDEX idx_lgd_tournament_id (tournament_id),
    INDEX idx_lgd_status (status)
);

CREATE TABLE IF NOT EXISTS league_game_day_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_day_id BIGINT NOT NULL,
    group_number INT NOT NULL,
    FOREIGN KEY (game_day_id) REFERENCES league_game_day(id) ON DELETE CASCADE,
    INDEX idx_lgdg_game_day_id (game_day_id)
);

CREATE TABLE IF NOT EXISTS league_game_day_group_player (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    tournament_player_id BIGINT NOT NULL,
    UNIQUE KEY uk_group_player (group_id, tournament_player_id),
    FOREIGN KEY (group_id) REFERENCES league_game_day_group(id) ON DELETE CASCADE,
    FOREIGN KEY (tournament_player_id) REFERENCES tournament_players(id) ON DELETE CASCADE
);

