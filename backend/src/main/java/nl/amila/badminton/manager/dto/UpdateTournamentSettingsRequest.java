package nl.amila.badminton.manager.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateTournamentSettingsRequest {
    // LEAGUE fields (required when tournament type is LEAGUE):
    private Integer k;
    private Integer absenteeDemerit;
    // ONE_OFF fields (required when tournament type is ONE_OFF):
    private Integer numberOfRounds;
    private Integer maxPoints;
}
