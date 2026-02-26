-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    role VARCHAR(50) NOT NULL DEFAULT 'PLAYER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
);

-- Create tournament table
CREATE TABLE IF NOT EXISTS tournament (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    owner_id BIGINT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    type VARCHAR(20) NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    FOREIGN KEY (owner_id) REFERENCES users(id),
    INDEX idx_owner_id (owner_id),
    INDEX idx_name (name)
);

-- Create tournament_admins junction table
CREATE TABLE IF NOT EXISTS tournament_admins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    UNIQUE KEY uk_tournament_user (tournament_id, user_id),
    FOREIGN KEY (tournament_id) REFERENCES tournament(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create tournament_players junction table
CREATE TABLE IF NOT EXISTS tournament_players (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
    status_changed_at BIGINT NOT NULL,
    `rank` INT NULL,
    rank_score DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    UNIQUE KEY uk_tournament_user (tournament_id, user_id),
    FOREIGN KEY (tournament_id) REFERENCES tournament(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_status (status)
);

-- Create league_game_day table
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

-- Create league_game_day_group table
CREATE TABLE IF NOT EXISTS league_game_day_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_day_id BIGINT NOT NULL,
    group_number INT NOT NULL,
    FOREIGN KEY (game_day_id) REFERENCES league_game_day(id) ON DELETE CASCADE,
    INDEX idx_lgdg_game_day_id (game_day_id)
);

-- Create league_game_day_group_player table
CREATE TABLE IF NOT EXISTS league_game_day_group_player (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    tournament_player_id BIGINT NOT NULL,
    UNIQUE KEY uk_group_player (group_id, tournament_player_id),
    FOREIGN KEY (group_id) REFERENCES league_game_day_group(id) ON DELETE CASCADE,
    FOREIGN KEY (tournament_player_id) REFERENCES tournament_players(id) ON DELETE CASCADE
);

-- Create tournament_league_settings table
CREATE TABLE IF NOT EXISTS tournament_league_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    ranking_logic VARCHAR(50) NOT NULL,
    ranking_config TEXT NULL,
    UNIQUE KEY uk_league_settings_tournament (tournament_id),
    FOREIGN KEY (tournament_id) REFERENCES tournament(id) ON DELETE CASCADE
);

-- Create tournament_one_off_settings table
CREATE TABLE IF NOT EXISTS tournament_one_off_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    number_of_rounds INT NOT NULL DEFAULT 1,
    max_points INT NOT NULL DEFAULT 21,
    UNIQUE KEY uk_one_off_settings_tournament (tournament_id),
    FOREIGN KEY (tournament_id) REFERENCES tournament(id) ON DELETE CASCADE
);

-- Create league_game_day_group_match table
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

-- Create rank_score_history table
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

