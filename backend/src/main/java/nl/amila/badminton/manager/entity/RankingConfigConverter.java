package nl.amila.badminton.manager.entity;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class RankingConfigConverter implements AttributeConverter<RankingConfig, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(RankingConfig config) {
        if (config == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(config);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Cannot serialize RankingConfig", e);
        }
    }

    @Override
    public RankingConfig convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, RankingConfig.class);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Cannot deserialize RankingConfig: " + json, e);
        }
    }
}
