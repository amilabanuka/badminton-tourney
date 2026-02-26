package nl.amila.badminton.manager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "league_game_day_group_match")
@Getter
@Setter
@NoArgsConstructor
public class LeagueGameDayGroupMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private LeagueGameDayGroup group;

    @Column(name = "match_order", nullable = false)
    private int matchOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team1_player1_id", nullable = false)
    private LeagueGameDayGroupPlayer team1Player1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team1_player2_id", nullable = false)
    private LeagueGameDayGroupPlayer team1Player2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team2_player1_id", nullable = false)
    private LeagueGameDayGroupPlayer team2Player1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team2_player2_id", nullable = false)
    private LeagueGameDayGroupPlayer team2Player2;

    @Column(name = "team1_score")
    private Integer team1Score;

    @Column(name = "team2_score")
    private Integer team2Score;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    public LeagueGameDayGroupMatch(LeagueGameDayGroup group, int matchOrder,
                                   LeagueGameDayGroupPlayer team1Player1,
                                   LeagueGameDayGroupPlayer team1Player2,
                                   LeagueGameDayGroupPlayer team2Player1,
                                   LeagueGameDayGroupPlayer team2Player2) {
        this.group = group;
        this.matchOrder = matchOrder;
        this.team1Player1 = team1Player1;
        this.team1Player2 = team1Player2;
        this.team2Player1 = team2Player1;
        this.team2Player2 = team2Player2;
    }
}

