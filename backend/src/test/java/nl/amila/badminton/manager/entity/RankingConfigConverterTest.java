package nl.amila.badminton.manager.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RankingConfigConverterTest {

    private final RankingConfigConverter converter = new RankingConfigConverter();

    // --- convertToDatabaseColumn ---

    @Test
    void convertToDatabaseColumn_null_returnsNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToDatabaseColumn_modifiedEloConfig_producesJsonWithTypeDiscriminator() {
        ModifiedEloConfig config = new ModifiedEloConfig(32, 5);

        String json = converter.convertToDatabaseColumn(config);

        assertNotNull(json);
        assertTrue(json.contains("\"type\":\"MODIFIED_ELO\""), "JSON must include type discriminator");
        assertTrue(json.contains("\"k\":32"));
        assertTrue(json.contains("\"absenteeDemerit\":5"));
    }

    // --- convertToEntityAttribute ---

    @Test
    void convertToEntityAttribute_null_returnsNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttribute_blank_returnsNull() {
        assertNull(converter.convertToEntityAttribute("   "));
    }

    @Test
    void convertToEntityAttribute_modifiedEloJson_returnsModifiedEloConfig() {
        String json = "{\"type\":\"MODIFIED_ELO\",\"k\":32,\"absenteeDemerit\":5}";

        RankingConfig result = converter.convertToEntityAttribute(json);

        assertInstanceOf(ModifiedEloConfig.class, result);
        ModifiedEloConfig eloConfig = (ModifiedEloConfig) result;
        assertEquals(32, eloConfig.k());
        assertEquals(5, eloConfig.absenteeDemerit());
    }

    @Test
    void convertToEntityAttribute_invalidJson_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> converter.convertToEntityAttribute("not-valid-json"));
    }

    // --- round-trip ---

    @Test
    void roundTrip_modifiedEloConfig_preservesValues() {
        ModifiedEloConfig original = new ModifiedEloConfig(20, 10);

        String json = converter.convertToDatabaseColumn(original);
        RankingConfig restored = converter.convertToEntityAttribute(json);

        assertInstanceOf(ModifiedEloConfig.class, restored);
        ModifiedEloConfig restoredElo = (ModifiedEloConfig) restored;
        assertEquals(original.k(), restoredElo.k());
        assertEquals(original.absenteeDemerit(), restoredElo.absenteeDemerit());
    }
}
