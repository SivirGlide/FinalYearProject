package org.example.lib.common.modules.transactionmap;

import org.dflib.DataFrame;
import org.dflib.Series;

import java.util.HashMap;
import java.util.Map;

public class CountryFrequencyModule implements TransactionMapModule<Map<String, Integer>> {

    @Override
    public String getModuleName() {
        return "CountryFrequency";
    }

    @Override
    public Map<String, Integer> run(DataFrame df) {
        Map<String, Integer> frequency = new HashMap<>();

        Series<?> countrySeries = df.getColumn("countryoforigin");

        for (int i = 0; i < countrySeries.size(); i++) {
            Object val = countrySeries.get(i);
            if (val == null) continue;

            String country = val.toString();
            // getOrDefault returns 0 if the key isn't in the map yet,
            // then we add 1 and put it back
            frequency.put(country, frequency.getOrDefault(country, 0) + 1);
        }

        return frequency;
    }
}
