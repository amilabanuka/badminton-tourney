package nl.amila.badminton.manager.dto.apl;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.amila.badminton.manager.entity.RankingLogic;

@Getter
@Setter
@NoArgsConstructor
public class AplSettingsRequest {
    private RankingLogic rankingLogic;
    private Integer k;
    /** Comma-separated demerit point values per absence e.g. "10,8,5,3" */
    private String absenteeDemeritPoints;
    /** Number of absences before automatic deactivation (1–20) */
    private Integer deactivationCount;
}
