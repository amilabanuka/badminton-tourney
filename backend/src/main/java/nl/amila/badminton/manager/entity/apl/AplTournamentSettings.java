package nl.amila.badminton.manager.entity.apl;

import jakarta.persistence.*;
import nl.amila.badminton.manager.entity.Tournament;
import nl.amila.badminton.manager.entity.RankingLogic;
import nl.amila.badminton.manager.entity.RankingConfig;
import nl.amila.badminton.manager.entity.RankingConfigConverter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tournament_apl_settings")
@Getter
@Setter
@NoArgsConstructor
public class AplTournamentSettings {

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

    /** Comma-separated list of demerit point values per absence e.g. "10,8,5,3" */
    @Column(name = "absentee_demerit_points")
    private String absenteeDemeritPoints;

    /** Number of absences before a player is automatically deactivated */
    @Column(name = "deactivation_count")
    private Integer deactivationCount;

    public AplTournamentSettings(Tournament tournament, RankingLogic rankingLogic, RankingConfig rankingConfig,
                                  String absenteeDemeritPoints, Integer deactivationCount) {
        this.tournament = tournament;
        this.rankingLogic = rankingLogic;
        this.rankingConfig = rankingConfig;
        this.absenteeDemeritPoints = absenteeDemeritPoints;
        this.deactivationCount = deactivationCount;
    }
}
