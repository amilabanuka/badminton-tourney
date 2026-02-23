package nl.amila.badminton.manager.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

@Entity
@Table(name = "tournament_admins")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class TournamentAdmin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tournament_id", referencedColumnName = "id")
    private Tournament tournament;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    public TournamentAdmin(Tournament tournament, User user) {
        this.tournament = tournament;
        this.user = user;
    }
}
