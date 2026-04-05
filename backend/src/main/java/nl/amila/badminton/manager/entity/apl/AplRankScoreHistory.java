package nl.amila.badminton.manager.entity.apl;

import jakarta.persistence.*;
import nl.amila.badminton.manager.entity.TournamentPlayer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "apl_rank_score_history")
@Getter
@Setter
@NoArgsConstructor
public class AplRankScoreHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_player_id", nullable = false)
    private TournamentPlayer tournamentPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private AplGameDayGroupMatch match;

    @Column(name = "previous_score", nullable = false, precision = 10, scale = 2)
    private BigDecimal previousScore;

    @Column(name = "new_score", nullable = false, precision = 10, scale = 2)
    private BigDecimal newScore;

    @Column(name = "changed_at", nullable = false)
    private long changedAt;

    public AplRankScoreHistory(TournamentPlayer tournamentPlayer, AplGameDayGroupMatch match,
                               BigDecimal previousScore, BigDecimal newScore) {
        this.tournamentPlayer = tournamentPlayer;
        this.match = match;
        this.previousScore = previousScore;
        this.newScore = newScore;
        this.changedAt = System.currentTimeMillis();
    }
}
