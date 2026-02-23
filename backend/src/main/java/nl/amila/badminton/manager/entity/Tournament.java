package nl.amila.badminton.manager.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

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
