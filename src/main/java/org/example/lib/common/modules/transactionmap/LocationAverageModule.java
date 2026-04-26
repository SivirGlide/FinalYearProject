package org.example.lib.common.modules.transactionmap;

import org.dflib.DataFrame;
import org.dflib.Series;
import org.example.lib.transactionmapper.AggregationResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationAverageModule implements TransactionMapModule<AggregationResult> {

    @Override
    public String getModuleName() {
        return "LocationAverage";
    }

    @Override
    public AggregationResult run(DataFrame df) {
        Series<?> countrySeries = df.getColumn("countryoforigin");

        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < countrySeries.size(); i++) {
            Object val = countrySeries.get(i);
            if (val == null) continue;
            String country = val.toString();
            counts.put(country, counts.getOrDefault(country, 0) + 1);
        }

        int total = counts.values().stream().mapToInt(Integer::intValue).sum();

        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(counts.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        String[] countries   = new String[sorted.size()];
        Integer[] txCounts   = new Integer[sorted.size()];
        Double[] percentages = new Double[sorted.size()];

        for (int i = 0; i < sorted.size(); i++) {
            countries[i]   = sorted.get(i).getKey();
            txCounts[i]    = sorted.get(i).getValue();
            percentages[i] = total == 0 ? 0.0 : Math.round((sorted.get(i).getValue() * 1000.0 / total)) / 10.0;
        }

        DataFrame result = DataFrame.byColumn("Country", "TransactionCount", "Percentage")
                .of(Series.of(countries), Series.of(txCounts), Series.of(percentages));

        return new AggregationResult(result);
    }
}