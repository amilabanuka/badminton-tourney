package nl.amila.badminton.manager.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ModifiedEloConfig.class, name = "MODIFIED_ELO")
})
public sealed interface RankingConfig permits ModifiedEloConfig {
}
