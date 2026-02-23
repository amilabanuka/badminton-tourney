package nl.amila.badminton.manager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "league_game_day")
@Getter
@Setter
@NoArgsConstructor
public class LeagueGameDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Column(name = "game_date", nullable = false, columnDefinition = "DATE")
    private LocalDate gameDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameDayStatus status = GameDayStatus.PENDING;

    @Column(nullable = false)
    private long createdAt;

    @Column(nullable = false)
    private long updatedAt;

    @OneToMany(mappedBy = "gameDay", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("group_number ASC")
    private List<LeagueGameDayGroup> groups = new ArrayList<>();

    public LeagueGameDay(Tournament tournament, LocalDate gameDate) {
        this.tournament = tournament;
        this.gameDate = gameDate;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
}


