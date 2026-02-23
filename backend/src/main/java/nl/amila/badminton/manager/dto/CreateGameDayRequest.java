package nl.amila.badminton.manager.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateGameDayRequest {
    /** Date in YYYY-MM-DD format */
    private String gameDate;
    /** List of TournamentPlayer IDs (not user IDs) to include */
    private List<Long> playerIds;
}

