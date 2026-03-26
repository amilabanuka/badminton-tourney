package nl.amila.badminton.manager.dto.oneoff;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OneOffSettingsRequest {
    private Integer numberOfRounds;
    private Integer maxPoints;
}
