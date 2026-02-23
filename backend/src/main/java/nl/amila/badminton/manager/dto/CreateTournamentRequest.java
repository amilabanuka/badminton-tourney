package nl.amila.badminton.manager.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.amila.badminton.manager.entity.TournamentType;

@Getter
@Setter
@NoArgsConstructor
public class CreateTournamentRequest {
    private String name;
    private Long ownerId;
    private boolean enabled = true;
    private TournamentType type;

    public CreateTournamentRequest(String name, Long ownerId, boolean enabled, TournamentType type) {
        this.name = name;
        this.ownerId = ownerId;
        this.enabled = enabled;
        this.type = type;
    }
}



