package org.example.lib.common.modules.suspiciousactivityengine;

import org.dflib.DataFrame;

import java.util.HashMap;
import java.util.Map;

public class ClassificationModule implements SuspiciousActivityEngineModule {

    @Override
    public String getModuleName() {
        return "Classification";
    }

    @Override
    public HashMap<String, Object> run(DataFrame transaction,
                                       DataFrame customerProfile,
                                       Object transactionMapData) {

        HashMap<String, Object> output = new HashMap<>();
        output.put("Module Name", getModuleName());
        output.put("Module Ran", true);

        Object beneficiaryCountryRaw = transaction.getColumn("countryofbeneficiary").get(0);
        if (beneficiaryCountryRaw == null) {
            output.put("Risk Score", 0);
            output.put("Comments", "countryofbeneficiary was null — could not score transaction.");
            return output;
        }

        String beneficiaryCountry = beneficiaryCountryRaw.toString();

        if (!(transactionMapData instanceof HashMap<?, ?> transactionMap)) {
            output.put("Risk Score", 0);
            output.put("Comments", "TransactionMap data was not a HashMap — could not score transaction.");
            return output;
        }

        Object frequencyRaw = transactionMap.get("CountryFrequency");
        Map<?, ?> countryFrequency = (frequencyRaw instanceof Map<?, ?> m) ? m : null;

        if (countryFrequency == null || countryFrequency.isEmpty()) {
            output.put("Risk Score", 0);
            output.put("Comments", "No CountryFrequency data available — could not score transaction.");
            return output;
        }

        Object rawCount = countryFrequency.get(beneficiaryCountry);
        if (rawCount == null) {
            output.put("Risk Score", 0);
            output.put("Comments", String.format(
                    "Beneficiary country '%s' has never appeared in customer's transaction history.", beneficiaryCountry));
            return output;
        }

        int countryCount = ((Number) rawCount).intValue();
        int maxCount = countryFrequency.values().stream()
                .mapToInt(v -> ((Number) v).intValue())
                .max()
                .orElse(1);

        int score = (int) Math.round((double) countryCount / maxCount * 100);

        output.put("Risk Score", score);
        output.put("Comments", String.format(
                "Beneficiary country '%s' seen in %d of %d past transactions (score: %d/100).",
                beneficiaryCountry, countryCount, maxCount, score));

        return output;
    }
}