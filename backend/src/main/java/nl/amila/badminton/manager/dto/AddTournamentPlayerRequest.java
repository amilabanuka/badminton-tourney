package nl.amila.badminton.manager.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class AddTournamentPlayerRequest {
    private Long userId;
    private BigDecimal rankScore;

    public AddTournamentPlayerRequest(Long userId) {
        this.userId = userId;
    }

    public AddTournamentPlayerRequest(Long userId, BigDecimal rankScore) {
        this.userId = userId;
        this.rankScore = rankScore;
    }
}

