-- Migration: Add APL tournament type tables
-- Run this against an existing database to add APL support.

CREATE TABLE IF NOT EXISTS tournament_apl_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    ranking_logic VARCHAR(50) NOT NULL,
    ranking_config TEXT NULL,
    absentee_demerit_points VARCHAR(255) NULL,
    deactivation_count INT NULL,
    UNIQUE KEY uk_apl_settings_tournament (tournament_id),
    FOREIGN KEY (tournament_id) REFERENCES tournament(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS apl_game_day (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    game_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    UNIQUE KEY uk_apl_tournament_date (tournament_id, game_date),
    FOREIGN KEY (tournament_id) REFERENCES tournament(id) ON DELETE CASCADE,
    INDEX idx_agd_tournament_id (tournament_id),
    INDEX idx_agd_status (status)
);

CREATE TABLE IF NOT EXISTS apl_game_day_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_day_id BIGINT NOT NULL,
    group_number INT NOT NULL,
    FOREIGN KEY (game_day_id) REFERENCES apl_game_day(id) ON DELETE CASCADE,
    INDEX idx_agdg_game_day_id (game_day_id)
);

CREATE TABLE IF NOT EXISTS apl_game_day_group_player (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    tournament_player_id BIGINT NOT NULL,
    UNIQUE KEY uk_apl_group_player (group_id, tournament_player_id),
    FOREIGN KEY (group_id) REFERENCES apl_game_day_group(id) ON DELETE CASCADE,
    FOREIGN KEY (tournament_player_id) REFERENCES tournament_players(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS apl_game_day_group_match (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    match_order INT NOT NULL,
    team1_player1_id BIGINT NOT NULL,
    team1_player2_id BIGINT NOT NULL,
    team2_player1_id BIGINT NOT NULL,
    team2_player2_id BIGINT NOT NULL,
    team1_score INT NULL,
    team2_score INT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (group_id) REFERENCES apl_game_day_group(id) ON DELETE CASCADE,
    FOREIGN KEY (team1_player1_id) REFERENCES apl_game_day_group_player(id) ON DELETE CASCADE,
    FOREIGN KEY (team1_player2_id) REFERENCES apl_game_day_group_player(id) ON DELETE CASCADE,
    FOREIGN KEY (team2_player1_id) REFERENCES apl_game_day_group_player(id) ON DELETE CASCADE,
    FOREIGN KEY (team2_player2_id) REFERENCES apl_game_day_group_player(id) ON DELETE CASCADE,
    INDEX idx_agdgm_group_id (group_id)
);

CREATE TABLE IF NOT EXISTS apl_rank_score_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_player_id BIGINT NOT NULL,
    match_id BIGINT NOT NULL,
    previous_score DECIMAL(10, 2) NOT NULL,
    new_score DECIMAL(10, 2) NOT NULL,
    changed_at BIGINT NOT NULL,
    FOREIGN KEY (tournament_player_id) REFERENCES tournament_players(id) ON DELETE CASCADE,
    FOREIGN KEY (match_id) REFERENCES apl_game_day_group_match(id) ON DELETE CASCADE,
    INDEX idx_arsh_tournament_player_id (tournament_player_id),
    INDEX idx_arsh_match_id (match_id)
);
