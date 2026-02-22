package nl.amila.badminton.manager.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateTournamentRequest {
    private String name;
    private Long ownerId;
    private boolean enabled = true;

    public CreateTournamentRequest(String name, Long ownerId, boolean enabled) {
        this.name = name;
        this.ownerId = ownerId;
        this.enabled = enabled;
    }
}

