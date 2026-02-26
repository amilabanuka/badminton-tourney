CREATE TABLE IF NOT EXISTS tournament_league_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    ranking_logic VARCHAR(50) NOT NULL,
    ranking_config TEXT NULL,
    UNIQUE KEY uk_league_settings_tournament (tournament_id),
    FOREIGN KEY (tournament_id) REFERENCES tournament(id) ON DELETE CASCADE
);
