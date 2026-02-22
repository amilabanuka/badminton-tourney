package nl.amila.badminton.manager.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthResponse {
    private boolean success;
    private String message;
    private UserDto user;
    private String token;

    // Constructors
    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AuthResponse(boolean success, String message, UserDto user, String token) {
        this.success = success;
        this.message = message;
        this.user = user;
        this.token = token;
    }

    // Inner class for user data
    @Getter
    @Setter
    @NoArgsConstructor
    public static class UserDto {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String role;

        public UserDto(Long id, String username, String email, String firstName, String lastName) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.role = "PLAYER";
        }

        public UserDto(Long id, String username, String email, String firstName, String lastName, String role) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.role = role;
        }
    }
}
