package nl.amila.badminton.manager.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

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

    public TournamentPlayer(Tournament tournament, User user) {
        this.tournament = tournament;
        this.user = user;
        this.status = PlayerStatus.ENABLED;
        this.statusChangedAt = System.currentTimeMillis();
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
        this.statusChangedAt = System.currentTimeMillis();
    }
}
