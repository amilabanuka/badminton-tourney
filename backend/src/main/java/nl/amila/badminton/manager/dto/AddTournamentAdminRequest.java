package nl.amila.badminton.manager.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddTournamentAdminRequest {
    private Long userId;

    public AddTournamentAdminRequest(Long userId) {
        this.userId = userId;
    }
}

