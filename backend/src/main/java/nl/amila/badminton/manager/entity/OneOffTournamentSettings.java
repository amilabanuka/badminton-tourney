package nl.amila.badminton.manager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tournament_one_off_settings")
@Getter
@Setter
@NoArgsConstructor
public class OneOffTournamentSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false, unique = true)
    private Tournament tournament;

    @Column(nullable = false)
    private int numberOfRounds = 1;

    @Column(nullable = false)
    private int maxPoints = 21;

    public OneOffTournamentSettings(Tournament tournament, int numberOfRounds, int maxPoints) {
        this.tournament = tournament;
        this.numberOfRounds = numberOfRounds;
        this.maxPoints = maxPoints;
    }
}
