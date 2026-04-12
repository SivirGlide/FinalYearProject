package org.example.lib.common.modules.transactionmap;

import org.dflib.DataFrame;
import org.dflib.Series;

import java.util.HashMap;
import java.util.Map;

/**
 * Example module — counts how many transactions originated from each country.
 *
 * This exists to show developers exactly how to build their own modules.
 * The pattern is always the same:
 *   1. Implement TransactionMapModule<T> with T being your return type
 *   2. Return a unique name from getModuleName()
 *   3. Do your DataFrame work in run() and return the result
 *   4. Never modify the DataFrame
 *
 * This module returns Map<String, Integer> — a country code mapped to
 * the number of transactions from that country.
 *
 * USAGE:
 *   TransactionMapResult results = new TransactionMap(df)
 *       .addModule(new CountryFrequencyModule())
 *       .run();
 *
 *   results.get("CountryFrequency", Map.class)
 *          .ifPresent(freq -> System.out.println("Frequencies: " + freq));
 */
public class CountryFrequencyModule implements TransactionMapModule<Map<String, Integer>> {

    @Override
    public String getModuleName() {
        return "CountryFrequency";
    }

    /**
     * Counts transactions per country of origin.
     *
     * @param df  The customer's transaction DataFrame.
     * @return    A Map of country code → transaction count, e.g. {"GB": 12, "US": 4}
     */
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
