package nl.amila.badminton.manager.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GameDayResponse {
    private boolean success;
    private String message;
    private GameDayDto gameDay;
    private List<GameDayDto> gameDays;

    public GameDayResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public GameDayResponse(boolean success, String message, GameDayDto gameDay) {
        this.success = success;
        this.message = message;
        this.gameDay = gameDay;
    }

    public GameDayResponse(boolean success, String message, List<GameDayDto> gameDays) {
        this.success = success;
        this.message = message;
        this.gameDays = gameDays;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class GameDayDto {
        private Long id;
        private Long tournamentId;
        private String gameDate;
        private String status;
        private long createdAt;
        private long updatedAt;
        private List<GroupDto> groups;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class GroupDto {
        private Long id;
        private int groupNumber;
        private List<GroupPlayerDto> players;
        private List<MatchDto> matches;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class MatchDto {
        private Long id;
        private int matchOrder;
        private Long team1Player1Id;
        private String team1Player1Name;
        private Long team1Player2Id;
        private String team1Player2Name;
        private Long team2Player1Id;
        private String team2Player1Name;
        private Long team2Player2Id;
        private String team2Player2Name;
        private Integer team1Score;
        private Integer team2Score;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class GroupPlayerDto {
        private Long tournamentPlayerId;
        private Long userId;
        private String firstName;
        private String lastName;
        private BigDecimal rankScore;
    }
}

