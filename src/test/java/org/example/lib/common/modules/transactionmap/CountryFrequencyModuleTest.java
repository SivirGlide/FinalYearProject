package org.example.lib.common.modules.transactionmap;

import org.dflib.DataFrame;
import org.dflib.Series;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CountryFrequencyModuleTest {

    private final CountryFrequencyModule module = new CountryFrequencyModule();

    @Test
    void getModuleName_returnsCountryFrequency() {
        assertEquals("CountryFrequency", module.getModuleName());
    }

    @Test
    void run_multipleCountries_returnsCorrectFrequencies() {
        DataFrame input = DataFrame.byColumn("countryoforigin")
                .of(Series.of("GB", "US", "GB", "FR", "US", "GB"));

        Map<String, Integer> result = module.run(input);

        assertEquals(3, result.get("GB"));
        assertEquals(2, result.get("US"));
        assertEquals(1, result.get("FR"));
        assertEquals(3, result.size());
    }

    @Test
    void run_withNullValues_ignoresNulls() {
        DataFrame input = DataFrame.byColumn("countryoforigin")
                .of(Series.of("GB", null, "US", null, "GB"));

        Map<String, Integer> result = module.run(input);

        assertEquals(2, result.get("GB"));
        assertEquals(1, result.get("US"));
        assertEquals(2, result.size());
    }

    @Test
    void run_emptyColumn_returnsEmptyMap() {
        DataFrame input = DataFrame.byColumn("countryoforigin")
                .of(Series.of());

        Map<String, Integer> result = module.run(input);

        assertTrue(result.isEmpty());
    }

    @Test
    void run_allNullValues_returnsEmptyMap() {
        DataFrame input = DataFrame.byColumn("countryoforigin")
                .of(Series.of(null, null));

        Map<String, Integer> result = module.run(input);

        assertTrue(result.isEmpty());
    }

    @Test
    void run_nonStringValues_convertsToStringKeys() {
        DataFrame input = DataFrame.byColumn("countryoforigin")
                .of(Series.of(123, 123, 456));

        Map<String, Integer> result = module.run(input);

        assertEquals(2, result.get("123"));
        assertEquals(1, result.get("456"));
    }

    @Test
    void run_missingColumn_throwsException() {
        DataFrame input = DataFrame.byColumn("otherColumn")
                .of(Series.of("GB", "US"));

        assertThrows(Exception.class, () -> module.run(input));
    }
}
