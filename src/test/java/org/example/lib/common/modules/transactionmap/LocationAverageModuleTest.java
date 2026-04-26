package org.example.lib.common.modules.transactionmap;

import org.dflib.DataFrame;
import org.dflib.Series;
import org.example.lib.transactionmapper.AggregationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocationAverageModuleTest {

    private final LocationAverageModule module = new LocationAverageModule();

    @Test
    void getModuleName_returnsLocationAverage() {
        assertEquals("LocationAverage", module.getModuleName());
    }

    @Test
    void run_multipleCountries_returnsSortedCountsAndPercentages() {
        DataFrame input = DataFrame.byColumn("countryoforigin")
                .of(Series.of("GB", "US", "GB", "FR", "GB", "US"));

        AggregationResult result = module.run(input);
        DataFrame df = result.getData();

        assertEquals("GB",  df.getColumn("Country").get(0));
        assertEquals(3,     df.getColumn("TransactionCount").get(0));
        assertEquals(50.0,  df.getColumn("Percentage").get(0));

        assertEquals("US",  df.getColumn("Country").get(1));
        assertEquals(2,     df.getColumn("TransactionCount").get(1));
        assertEquals(33.3,  df.getColumn("Percentage").get(1));

        assertEquals("FR",  df.getColumn("Country").get(2));
        assertEquals(1,     df.getColumn("TransactionCount").get(2));
        assertEquals(16.7,  df.getColumn("Percentage").get(2));
    }

    @Test
    void run_withNullValues_ignoresNullsInCalculation() {
        DataFrame input = DataFrame.byColumn("countryoforigin")
                .of(Series.of("GB", null, "GB", "US", null));

        AggregationResult result = module.run(input);
        DataFrame df = result.getData();

        assertEquals(2,    df.getColumn("TransactionCount").get(0)); // GB
        assertEquals(66.7, df.getColumn("Percentage").get(0));

        assertEquals(1,    df.getColumn("TransactionCount").get(1)); // US
        assertEquals(33.3, df.getColumn("Percentage").get(1));
    }

    @Test
    void run_emptyColumn_returnsEmptyDataFrame() {
        DataFrame input = DataFrame.byColumn("countryoforigin")
                .of(Series.of());

        AggregationResult result = module.run(input);
        DataFrame df = result.getData();

        assertEquals(0, df.height());
    }

    @Test
    void run_allNullValues_returnsEmptyDataFrame() {
        DataFrame input = DataFrame.byColumn("countryoforigin")
                .of(Series.of(null, null));

        AggregationResult result = module.run(input);
        DataFrame df = result.getData();

        assertEquals(0, df.height());
    }

    @Test
    void run_singleCountry_returns100Percent() {
        DataFrame input = DataFrame.byColumn("countryoforigin")
                .of(Series.of("GB", "GB", "GB"));

        AggregationResult result = module.run(input);
        DataFrame df = result.getData();

        assertEquals(1,     df.height());
        assertEquals("GB",  df.getColumn("Country").get(0));
        assertEquals(3,     df.getColumn("TransactionCount").get(0));
        assertEquals(100.0, df.getColumn("Percentage").get(0));
    }

    @Test
    void run_nonStringValues_convertedToString() {
        DataFrame input = DataFrame.byColumn("countryoforigin")
                .of(Series.of(1, 1, 2));

        AggregationResult result = module.run(input);
        DataFrame df = result.getData();

        assertEquals("1", df.getColumn("Country").get(0));
        assertEquals(2,   df.getColumn("TransactionCount").get(0));
    }

    @Test
    void run_missingColumn_throwsException() {
        DataFrame input = DataFrame.byColumn("otherColumn")
                .of(Series.of("GB", "US"));

        assertThrows(Exception.class, () -> module.run(input));
    }
}
