package nl.amila.badminton.manager.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {
    private String username;
    private String password;

    // Constructors
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}

