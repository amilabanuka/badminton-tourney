package nl.amila.badminton.manager.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tournament")
@Getter
@Setter
@NoArgsConstructor
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Long ownerId;
    private boolean enabled = true;
    private long createdAt;
    private long updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    @Setter(lombok.AccessLevel.NONE)
    private TournamentType type;

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TournamentAdmin> admins = new ArrayList<>();

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TournamentPlayer> players = new ArrayList<>();

    @OneToOne(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private LeagueTournamentSettings leagueSettings;

    @OneToOne(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private OneOffTournamentSettings oneOffSettings;

    public Object getSettings() {
        return switch (type) {
            case LEAGUE -> leagueSettings;
            case ONE_OFF -> oneOffSettings;
        };
    }

    public Tournament(String name, Long ownerId, boolean enabled, TournamentType type) {
        this.name = name;
        this.ownerId = ownerId;
        this.enabled = enabled;
        this.type = type;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.admins = new ArrayList<>();
        this.players = new ArrayList<>();
    }
}
