package nl.amila.badminton.manager.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.amila.badminton.manager.entity.TournamentType;

import java.util.List;

/**
 * Lightweight tournament response for PLAYER role — contains no admin-sensitive data.
 */
@Getter
@Setter
@NoArgsConstructor
public class PlayerTournamentResponse {
    private boolean success;
    private String message;
    private TournamentDto tournament;
    private List<TournamentDto> tournaments;

    public PlayerTournamentResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public PlayerTournamentResponse(boolean success, String message, TournamentDto tournament) {
        this.success = success;
        this.message = message;
        this.tournament = tournament;
    }

    public PlayerTournamentResponse(boolean success, String message, List<TournamentDto> tournaments) {
        this.success = success;
        this.message = message;
        this.tournaments = tournaments;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TournamentDto {
        private Long id;
        private String name;
        private TournamentType type;
        private boolean enabled;
        private List<GameDaySummaryDto> gameDays;

        public TournamentDto(Long id, String name, TournamentType type, boolean enabled) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.enabled = enabled;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class GameDaySummaryDto {
        private Long id;
        private String gameDate;
        private String status;

        public GameDaySummaryDto(Long id, String gameDate, String status) {
            this.id = id;
            this.gameDate = gameDate;
            this.status = status;
        }
    }
}

