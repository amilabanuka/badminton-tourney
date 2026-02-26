package nl.amila.badminton.manager.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.amila.badminton.manager.entity.RankingLogic;

@Getter
@Setter
@NoArgsConstructor
public class LeagueSettingsRequest {
    private RankingLogic rankingLogic;
    private Integer k;
    private Integer absenteeDemerit;
}
