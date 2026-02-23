package nl.amila.badminton.manager.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.amila.badminton.manager.entity.TournamentType;

import java.math.BigDecimal;
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
        private TournamentType type;
        private List<Long> adminIds;
        private List<Long> playerIds;
        private List<AdminDto> admins;
        private List<PlayerDto> players;

        public TournamentDto(Long id, String name, Long ownerId, boolean enabled, long createdAt, long updatedAt, TournamentType type) {
            this.id = id;
            this.name = name;
            this.ownerId = ownerId;
            this.enabled = enabled;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.type = type;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class AdminDto {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;

        public AdminDto(Long id, String firstName, String lastName, String email) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PlayerDto {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String status;
        private long statusChangedAt;
        private Integer rank;
        private BigDecimal rankScore;

        public PlayerDto(Long id, String firstName, String lastName, String email, String status, long statusChangedAt, Integer rank, BigDecimal rankScore) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.status = status;
            this.statusChangedAt = statusChangedAt;
            this.rank = rank;
            this.rankScore = rankScore;
        }
    }
}

