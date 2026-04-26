package org.example.lib.common.modules.suspiciousactivityengine;

import org.dflib.DataFrame;
import org.dflib.Series;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ClassificationModuleTest {

    private final ClassificationModule module = new ClassificationModule();

    @Test
    void getModuleName_returnsClassification() {
        assertEquals("Classification", module.getModuleName());
    }

    @Test
    void run_beneficiaryCountryIsMostUsedCountry_returnsRiskScore100() {
        DataFrame transaction = DataFrame.byColumn("countryofbeneficiary")
                .of(Series.of("GB"));

        HashMap<String, Object> transactionMapData = new HashMap<>();
        transactionMapData.put("CountryFrequency", Map.of("GB", 10, "US", 3, "FR", 2));

        HashMap<String, Object> result = module.run(transaction, null, transactionMapData);

        assertEquals("Classification", result.get("Module Name"));
        assertEquals(true,             result.get("Module Ran"));
        assertEquals(100,              result.get("Risk Score"));
        assertEquals(
                "Beneficiary country 'GB' seen in 10 of 10 past transactions (score: 100/100).",
                result.get("Comments")
        );
    }

    @Test
    void run_beneficiaryCountryHasLowerFrequency_returnsProportionalRiskScore() {
        DataFrame transaction = DataFrame.byColumn("countryofbeneficiary")
                .of(Series.of("US"));

        HashMap<String, Object> transactionMapData = new HashMap<>();
        transactionMapData.put("CountryFrequency", Map.of("GB", 10, "US", 4));

        HashMap<String, Object> result = module.run(transaction, null, transactionMapData);

        assertEquals(40, result.get("Risk Score"));
        assertEquals(
                "Beneficiary country 'US' seen in 4 of 10 past transactions (score: 40/100).",
                result.get("Comments")
        );
    }

    @Test
    void run_beneficiaryCountryHasRoundedFrequencyScore_returnsRoundedRiskScore() {
        DataFrame transaction = DataFrame.byColumn("countryofbeneficiary")
                .of(Series.of("US"));

        HashMap<String, Object> transactionMapData = new HashMap<>();
        transactionMapData.put("CountryFrequency", Map.of("GB", 3, "US", 2));

        HashMap<String, Object> result = module.run(transaction, null, transactionMapData);

        assertEquals(67, result.get("Risk Score"));
    }

    @Test
    void run_beneficiaryCountryHasNeverAppeared_returnsRiskScoreZero() {
        DataFrame transaction = DataFrame.byColumn("countryofbeneficiary")
                .of(Series.of("DE"));

        HashMap<String, Object> transactionMapData = new HashMap<>();
        transactionMapData.put("CountryFrequency", Map.of("GB", 10, "US", 4));

        HashMap<String, Object> result = module.run(transaction, null, transactionMapData);

        assertEquals(0, result.get("Risk Score"));
        assertEquals(
                "Beneficiary country 'DE' has never appeared in customer's transaction history.",
                result.get("Comments")
        );
    }

    @Test
    void run_beneficiaryCountryIsNull_returnsRiskScoreZero() {
        DataFrame transaction = DataFrame.byColumn("countryofbeneficiary")
                .of(Series.of((Object) null));

        HashMap<String, Object> transactionMapData = new HashMap<>();
        transactionMapData.put("CountryFrequency", Map.of("GB", 10));

        HashMap<String, Object> result = module.run(transaction, null, transactionMapData);

        assertEquals("Classification", result.get("Module Name"));
        assertEquals(true,             result.get("Module Ran"));
        assertEquals(0,                result.get("Risk Score"));
        assertEquals(
                "countryofbeneficiary was null — could not score transaction.",
                result.get("Comments")
        );
    }

    @Test
    void run_transactionMapDataIsNotHashMap_returnsRiskScoreZero() {
        DataFrame transaction = DataFrame.byColumn("countryofbeneficiary")
                .of(Series.of("GB"));

        HashMap<String, Object> result = module.run(transaction, null, "invalid data");

        assertEquals(0, result.get("Risk Score"));
        assertEquals(
                "TransactionMap data was not a HashMap — could not score transaction.",
                result.get("Comments")
        );
    }

    @Test
    void run_countryFrequencyMissing_returnsRiskScoreZero() {
        DataFrame transaction = DataFrame.byColumn("countryofbeneficiary")
                .of(Series.of("GB"));

        HashMap<String, Object> result = module.run(transaction, null, new HashMap<>());

        assertEquals(0, result.get("Risk Score"));
        assertEquals(
                "No CountryFrequency data available — could not score transaction.",
                result.get("Comments")
        );
    }

    @Test
    void run_countryFrequencyIsNotMap_returnsRiskScoreZero() {
        DataFrame transaction = DataFrame.byColumn("countryofbeneficiary")
                .of(Series.of("GB"));

        HashMap<String, Object> transactionMapData = new HashMap<>();
        transactionMapData.put("CountryFrequency", "invalid frequency data");

        HashMap<String, Object> result = module.run(transaction, null, transactionMapData);

        assertEquals(0, result.get("Risk Score"));
        assertEquals(
                "No CountryFrequency data available — could not score transaction.",
                result.get("Comments")
        );
    }

    @Test
    void run_countryFrequencyIsEmpty_returnsRiskScoreZero() {
        DataFrame transaction = DataFrame.byColumn("countryofbeneficiary")
                .of(Series.of("GB"));

        HashMap<String, Object> transactionMapData = new HashMap<>();
        transactionMapData.put("CountryFrequency", Map.of());

        HashMap<String, Object> result = module.run(transaction, null, transactionMapData);

        assertEquals(0, result.get("Risk Score"));
        assertEquals(
                "No CountryFrequency data available — could not score transaction.",
                result.get("Comments")
        );
    }

    @Test
    void run_nonStringBeneficiaryCountry_convertsCountryToString() {
        DataFrame transaction = DataFrame.byColumn("countryofbeneficiary")
                .of(Series.of(123));

        HashMap<String, Object> transactionMapData = new HashMap<>();
        transactionMapData.put("CountryFrequency", Map.of("123", 2, "456", 4));

        HashMap<String, Object> result = module.run(transaction, null, transactionMapData);

        assertEquals(50, result.get("Risk Score"));
        assertEquals(
                "Beneficiary country '123' seen in 2 of 4 past transactions (score: 50/100).",
                result.get("Comments")
        );
    }

    @Test
    void run_countryFrequencyContainsNonNumericValue_throwsClassCastException() {
        DataFrame transaction = DataFrame.byColumn("countryofbeneficiary")
                .of(Series.of("GB"));

        HashMap<String, Object> transactionMapData = new HashMap<>();
        transactionMapData.put("CountryFrequency", Map.of("GB", "invalid", "US", 4));

        assertThrows(
                ClassCastException.class,
                () -> module.run(transaction, null, transactionMapData)
        );
    }

    @Test
    void run_missingCountryOfBeneficiaryColumn_throwsException() {
        DataFrame transaction = DataFrame.byColumn("otherColumn")
                .of(Series.of("GB"));

        HashMap<String, Object> transactionMapData = new HashMap<>();
        transactionMapData.put("CountryFrequency", Map.of("GB", 1));

        assertThrows(Exception.class, () -> module.run(transaction, null, transactionMapData));
    }
}
