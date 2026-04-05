package nl.amila.badminton.manager.dto.apl;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AplPlayerHistoryResponse {
    private boolean success;
    private String message;
    private Long tournamentPlayerId;
    private String playerName;
    private List<GameDayHistoryDto> gameDays;

    public AplPlayerHistoryResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AplPlayerHistoryResponse(boolean success, String message,
                                     Long tournamentPlayerId, String playerName,
                                     List<GameDayHistoryDto> gameDays) {
        this.success = success;
        this.message = message;
        this.tournamentPlayerId = tournamentPlayerId;
        this.playerName = playerName;
        this.gameDays = gameDays;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class GameDayHistoryDto {
        private Long gameDayId;
        private String gameDate;
        private List<MatchHistoryDto> matches;

        public GameDayHistoryDto(Long gameDayId, String gameDate, List<MatchHistoryDto> matches) {
            this.gameDayId = gameDayId;
            this.gameDate = gameDate;
            this.matches = matches;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class MatchHistoryDto {
        private Long matchId;
        private int matchOrder;
        private String team1Player1Name;
        private String team1Player2Name;
        private String team2Player1Name;
        private String team2Player2Name;
        private Integer team1Score;
        private Integer team2Score;
        private boolean playerOnTeam1;
        private BigDecimal previousScore;
        private BigDecimal newScore;
        private BigDecimal scoreDelta;

        public MatchHistoryDto(Long matchId, int matchOrder,
                               String team1Player1Name, String team1Player2Name,
                               String team2Player1Name, String team2Player2Name,
                               Integer team1Score, Integer team2Score,
                               boolean playerOnTeam1,
                               BigDecimal previousScore, BigDecimal newScore) {
            this.matchId = matchId;
            this.matchOrder = matchOrder;
            this.team1Player1Name = team1Player1Name;
            this.team1Player2Name = team1Player2Name;
            this.team2Player1Name = team2Player1Name;
            this.team2Player2Name = team2Player2Name;
            this.team1Score = team1Score;
            this.team2Score = team2Score;
            this.playerOnTeam1 = playerOnTeam1;
            this.previousScore = previousScore;
            this.newScore = newScore;
            this.scoreDelta = newScore.subtract(previousScore);
        }
    }
}
