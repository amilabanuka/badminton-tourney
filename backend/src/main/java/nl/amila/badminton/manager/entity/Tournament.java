package nl.amila.badminton.manager.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("tournament")
@Getter
@Setter
@NoArgsConstructor
public class Tournament {
    @Id
    private Long id;
    private String name;
    private Long ownerId;
    private boolean enabled = true;
    private long createdAt;
    private long updatedAt;

    public Tournament(String name, Long ownerId, boolean enabled) {
        this.name = name;
        this.ownerId = ownerId;
        this.enabled = enabled;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
}

