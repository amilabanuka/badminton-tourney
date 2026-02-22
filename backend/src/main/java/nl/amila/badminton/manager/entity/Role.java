package nl.amila.badminton.manager.entity;

import lombok.Getter;

@Getter
public enum Role {
    ADMIN("Admin"),
    TOURNY_ADMIN("Tournament Admin"),
    PLAYER("Player");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }
}
