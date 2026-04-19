package org.example.lib.common.modules.ruleengine;

import org.dflib.DataFrame;
import org.example.lib.transactionmapper.TransactionMapResult;

import java.util.Map;

/**
 * Example module — scores a transaction against the customer's historical behaviour.
 *
 * Returns a double in [0.0, 1.0]:
 *   1.0 = transaction looks entirely consistent with past behaviour
 *   0.0 = transaction looks completely outside past behaviour
 *
 * Currently uses CountryFrequency from the TransactionMapResult to check whether
 * the beneficiary country has been seen before and how often relative to the
 * customer's most-used country.
 *
 * USAGE:
 *   RuleEngineResult results = new RuleEngine(transaction, customerProfile, transactionMapResult)
 *       .addModule(new ClassificationModule())
 *       .run();
 *
 *   results.get("Classification", Double.class)
 *          .ifPresent(score -> System.out.println("Score: " + score));
 */
public class ClassificationModule implements RuleEngineModule<Double> {

    @Override
    public String getModuleName() {
        return "Classification";
    }

    @Override
    public Double run(DataFrame transaction,
                      DataFrame customerProfile,
                      TransactionMapResult transactionMap) {

        Object beneficiaryCountryRaw = transaction.getColumn("countryofbeneficiary").get(0);
        if (beneficiaryCountryRaw == null) return 0.0;
        String beneficiaryCountry = beneficiaryCountryRaw.toString();

        Map<?, ?> countryFrequency = transactionMap
                .get("CountryFrequency", Map.class)
                .orElse(null);

        if (countryFrequency == null || countryFrequency.isEmpty()) return 0.0;

        Object rawCount = countryFrequency.get(beneficiaryCountry);
        if (rawCount == null) return 0.0;

        int countryCount = ((Number) rawCount).intValue();
        int maxCount = countryFrequency.values().stream()
                .mapToInt(v -> ((Number) v).intValue())
                .max()
                .orElse(1);

        return (double) countryCount / maxCount;
    }
}