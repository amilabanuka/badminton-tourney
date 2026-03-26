package nl.amila.badminton.manager.entity.league;

import jakarta.persistence.*;
import nl.amila.badminton.manager.entity.TournamentPlayer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "league_game_day_group_player")
@Getter
@Setter
@NoArgsConstructor
public class LeagueGameDayGroupPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private LeagueGameDayGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_player_id", nullable = false)
    private TournamentPlayer tournamentPlayer;

    public LeagueGameDayGroupPlayer(LeagueGameDayGroup group, TournamentPlayer tournamentPlayer) {
        this.group = group;
        this.tournamentPlayer = tournamentPlayer;
    }
}

