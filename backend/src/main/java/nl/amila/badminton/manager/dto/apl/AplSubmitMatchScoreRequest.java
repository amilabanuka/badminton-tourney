package nl.amila.badminton.manager.dto.apl;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AplSubmitMatchScoreRequest {
    private Integer team1Score;
    private Integer team2Score;
}
