package nl.amila.badminton.manager.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserListResponse {
    private boolean success;
    private String message;
    private List<UserListDto> users;

    public UserListResponse(boolean success, String message, List<UserListDto> users) {
        this.success = success;
        this.message = message;
        this.users = users;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class UserListDto {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;

        public UserListDto(Long id, String username, String email, String firstName, String lastName) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
        }
    }
}

