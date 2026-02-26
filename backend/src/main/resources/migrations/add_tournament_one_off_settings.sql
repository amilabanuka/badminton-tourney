CREATE TABLE IF NOT EXISTS tournament_one_off_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    number_of_rounds INT NOT NULL DEFAULT 1,
    max_points INT NOT NULL DEFAULT 21,
    UNIQUE KEY uk_one_off_settings_tournament (tournament_id),
    FOREIGN KEY (tournament_id) REFERENCES tournament(id) ON DELETE CASCADE
);
