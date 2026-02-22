package nl.amila.badminton.manager.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TournamentResponse {
    private boolean success;
    private String message;
    private TournamentDto tournament;
    private List<TournamentDto> tournaments;

    public TournamentResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public TournamentResponse(boolean success, String message, TournamentDto tournament) {
        this.success = success;
        this.message = message;
        this.tournament = tournament;
    }

    public TournamentResponse(boolean success, String message, List<TournamentDto> tournaments) {
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
        private Long ownerId;
        private boolean enabled;
        private long createdAt;
        private long updatedAt;
        private List<Long> adminIds;
        private List<Long> playerIds;

        public TournamentDto(Long id, String name, Long ownerId, boolean enabled, long createdAt, long updatedAt) {
            this.id = id;
            this.name = name;
            this.ownerId = ownerId;
            this.enabled = enabled;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
    }
}

