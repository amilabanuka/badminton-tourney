package nl.amila.badminton.manager.entity;

public record ModifiedEloConfig(int k, int absenteeDemerit) implements RankingConfig {
}
