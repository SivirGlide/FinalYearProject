package org.example.lib.common.modules.transactionmap;

import org.dflib.DataFrame;
import org.dflib.Series;
import org.example.lib.transactionmapper.AggregationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AmountAggregationModuleTest {

    private final AmountAggregationModule module = new AmountAggregationModule();

    @Test
    void getModuleName_returnsAmountAggregation() {
        assertEquals("AmountAggregation", module.getModuleName());
    }

    @Test
    void run_multiplePositiveAmounts_returnsCorrectAggregationValues() {
        DataFrame input = DataFrame.byColumn("amount")
                .of(Series.of(10.0, 20.0, 30.0));

        AggregationResult result = module.run(input);
        DataFrame df = result.getData();

        assertEquals(60.0,  df.getColumn("Total").get(0));
        assertEquals(20.0,  df.getColumn("Average").get(0));
        assertEquals(10.0,  df.getColumn("Min").get(0));
        assertEquals(30.0,  df.getColumn("Max").get(0));
    }

    @Test
    void run_amountsContainNulls_ignoresNullValues() {
        DataFrame input = DataFrame.byColumn("amount")
                .of(Series.of(10.0, null, 30.0));

        AggregationResult result = module.run(input);
        DataFrame df = result.getData();

        assertEquals(40.0,  df.getColumn("Total").get(0));
        assertEquals(20.0,  df.getColumn("Average").get(0));
        assertEquals(10.0,  df.getColumn("Min").get(0));
        assertEquals(30.0,  df.getColumn("Max").get(0));
    }

    @Test
    void run_emptyAmountColumn_returnsZeroAggregationValues() {
        DataFrame input = DataFrame.byColumn("amount")
                .of(Series.of());

        AggregationResult result = module.run(input);
        DataFrame df = result.getData();

        assertEquals(0.0, df.getColumn("Total").get(0));
        assertEquals(0.0, df.getColumn("Average").get(0));
        assertEquals(0.0, df.getColumn("Min").get(0));
        assertEquals(0.0, df.getColumn("Max").get(0));
    }

    @Test
    void run_amountColumnContainsOnlyNulls_returnsZeroAggregationValues() {
        DataFrame input = DataFrame.byColumn("amount")
                .of(Series.of(null, null, null));

        AggregationResult result = module.run(input);
        DataFrame df = result.getData();

        assertEquals(0.0, df.getColumn("Total").get(0));
        assertEquals(0.0, df.getColumn("Average").get(0));
        assertEquals(0.0, df.getColumn("Min").get(0));
        assertEquals(0.0, df.getColumn("Max").get(0));
    }

    @Test
    void run_mixedPositiveAndNegativeAmounts_returnsCorrectAggregationValues() {
        DataFrame input = DataFrame.byColumn("amount")
                .of(Series.of(-10.0, 20.0, -5.0, 15.0));

        AggregationResult result = module.run(input);
        DataFrame df = result.getData();

        assertEquals(20.0,  df.getColumn("Total").get(0));
        assertEquals(5.0,   df.getColumn("Average").get(0));
        assertEquals(-10.0, df.getColumn("Min").get(0));
        assertEquals(20.0,  df.getColumn("Max").get(0));
    }

    @Test
    void run_integerAmounts_returnsCorrectDoubleAggregationValues() {
        DataFrame input = DataFrame.byColumn("amount")
                .of(Series.of(5, 10, 15));

        AggregationResult result = module.run(input);
        DataFrame df = result.getData();

        assertEquals(30.0, df.getColumn("Total").get(0));
        assertEquals(10.0, df.getColumn("Average").get(0));
        assertEquals(5.0,  df.getColumn("Min").get(0));
        assertEquals(15.0, df.getColumn("Max").get(0));
    }

    @Test
    void run_missingAmountColumn_throwsException() {
        DataFrame input = DataFrame.byColumn("description")
                .of(Series.of("Coffee", "Lunch"));

        assertThrows(Exception.class, () -> module.run(input));
    }

    @Test
    void run_nonNumericAmountValue_throwsClassCastException() {
        DataFrame input = DataFrame.byColumn("amount")
                .of(Series.of(10.0, "invalid", 30.0));

        assertThrows(ClassCastException.class, () -> module.run(input));
    }
}
