package nl.amila.badminton.manager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

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
    @OrderBy("id ASC")
    private Set<LeagueGameDayGroupPlayer> players = new LinkedHashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("match_order ASC")
    private Set<LeagueGameDayGroupMatch> matches = new LinkedHashSet<>();

    public LeagueGameDayGroup(LeagueGameDay gameDay, int groupNumber) {
        this.gameDay = gameDay;
        this.groupNumber = groupNumber;
    }
}

