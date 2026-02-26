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
    private LeagueSettingsRequest leagueSettings;
    private OneOffSettingsRequest oneOffSettings;
}



