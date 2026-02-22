package nl.amila.badminton.manager.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddTournamentPlayerRequest {
    private Long userId;

    public AddTournamentPlayerRequest(Long userId) {
        this.userId = userId;
    }
}

