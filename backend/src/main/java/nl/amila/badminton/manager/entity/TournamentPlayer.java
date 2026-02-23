package nl.amila.badminton.manager.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tournament_players")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class TournamentPlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tournament_id", referencedColumnName = "id")
    private Tournament tournament;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerStatus status = PlayerStatus.ENABLED;

    @Column(nullable = false)
    private long statusChangedAt;

    @Column(name = "`rank`")
    private Integer rank;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal rankScore = BigDecimal.ZERO;

    public TournamentPlayer(Tournament tournament, User user) {
        this(tournament, user, BigDecimal.ZERO);
    }

    public TournamentPlayer(Tournament tournament, User user, BigDecimal rankScore) {
        this.tournament = tournament;
        this.user = user;
        this.statusChangedAt = System.currentTimeMillis();
        this.rankScore = rankScore != null ? rankScore : BigDecimal.ZERO;
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
        this.statusChangedAt = System.currentTimeMillis();
    }
}
