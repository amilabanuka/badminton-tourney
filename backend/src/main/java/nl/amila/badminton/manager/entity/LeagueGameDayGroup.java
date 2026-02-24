package nl.amila.badminton.manager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "league_game_day_group")
@Getter
@Setter
@NoArgsConstructor
public class LeagueGameDayGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_day_id", nullable = false)
    private LeagueGameDay gameDay;

    @Column(nullable = false)
    private int groupNumber;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LeagueGameDayGroupPlayer> players = new ArrayList<>();

    public LeagueGameDayGroup(LeagueGameDay gameDay, int groupNumber) {
        this.gameDay = gameDay;
        this.groupNumber = groupNumber;
    }
}

