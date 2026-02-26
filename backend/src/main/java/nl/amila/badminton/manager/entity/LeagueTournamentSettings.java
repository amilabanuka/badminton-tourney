package nl.amila.badminton.manager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tournament_league_settings")
@Getter
@Setter
@NoArgsConstructor
public class LeagueTournamentSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false, unique = true)
    private Tournament tournament;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RankingLogic rankingLogic;

    @Convert(converter = RankingConfigConverter.class)
    @Column(name = "ranking_config", columnDefinition = "TEXT")
    private RankingConfig rankingConfig;

    public LeagueTournamentSettings(Tournament tournament, RankingLogic rankingLogic, RankingConfig rankingConfig) {
        this.tournament = tournament;
        this.rankingLogic = rankingLogic;
        this.rankingConfig = rankingConfig;
    }
}
