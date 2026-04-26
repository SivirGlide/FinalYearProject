package org.example.lib.common.modules.transactionmap;

import org.dflib.DataFrame;
import org.dflib.Series;
import org.example.lib.transactionmapper.AggregationResult;


public class AmountAggregationModule implements TransactionMapModule<AggregationResult> {

    @Override
    public String getModuleName() {
        return "AmountAggregation";
    }

    @Override
    public AggregationResult run(DataFrame df) {
        Series<?> amountSeries = df.getColumn("amount");

        double total = 0;
        double min   = Double.MAX_VALUE;
        double max   = Double.MIN_VALUE;
        int    count = 0;

        for (int i = 0; i < amountSeries.size(); i++) {
            Object val = amountSeries.get(i);
            if (val == null) continue;
            double amount = ((Number) val).doubleValue();
            total += amount;
            if (amount < min) min = amount;
            if (amount > max) max = amount;
            count++;
        }

        double average = count == 0 ? 0 : total / count;
        if (count == 0) { min = 0; max = 0; }

        DataFrame result = DataFrame.byColumn("Total", "Average", "Min", "Max")
                .of(Series.of(total), Series.of(average), Series.of(min), Series.of(max));

        return new AggregationResult(result);
    }
}