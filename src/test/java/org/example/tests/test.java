package org.example.tests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dflib.DataFrame;
import org.dflib.Series;
import org.example.lib.common.modules.suspiciousactivityengine.ClassificationModule;
import org.example.lib.common.modules.transactionmap.AmountAggregationModule;
import org.example.lib.common.modules.transactionmap.CountryFrequencyModule;
import org.example.lib.common.modules.transactionmap.LocationAverageModule;
import org.example.lib.common.modules.transactionmap.TopBeneficiariesModule;
import org.example.lib.suspiciousactivityengine.SuspiciousActivityEngine;
import org.example.lib.suspiciousactivityengine.SuspiciousActivityEngineResult;
import org.example.lib.transactionmapper.AggregationResult;
import org.example.lib.transactionmapper.InformativeResult;
import org.example.lib.transactionmapper.TransactionMap;
import org.example.lib.validator.PersonalCustomerValidator;
import org.example.lib.validator.TransactionValidator;
import org.example.lib.validator.report.ValidationIssue;
import org.example.lib.validator.report.ValidationReport;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FraudPipelineE2ETest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void personalCustomerJourney_buildsTransactionMapAndReturnsExpectedRiskScore() throws Exception {
        DataFrame customerProfile = validPersonalCustomerProfile();
        DataFrame transactionHistory = validTransactionHistory();
        DataFrame incomingTransaction = validIncomingTransaction("US");

        ValidationReport customerReport = new PersonalCustomerValidator().validate(customerProfile);
        ValidationReport historyReport = new TransactionValidator().validate(transactionHistory);
        ValidationReport incomingReport = new TransactionValidator().validate(incomingTransaction);

        assertFalse(customerReport.hasErrors());
        assertFalse(customerReport.hasWarnings());
        assertFalse(historyReport.hasErrors());
        assertFalse(incomingReport.hasErrors());

        HashMap<String, Object> transactionMap = new TransactionMap(transactionHistory)
                .addModule(new CountryFrequencyModule())
                .addModule(new LocationAverageModule())
                .addModule(new AmountAggregationModule())
                .addModule(new TopBeneficiariesModule())
                .run();

        @SuppressWarnings("unchecked")
        Map<String, Integer> countryFrequency = (Map<String, Integer>) transactionMap.get("CountryFrequency");
        assertEquals(Map.of("GB", 3, "US", 2, "FR", 1), countryFrequency);

        AggregationResult locationAverage = (AggregationResult) transactionMap.get("LocationAverage");
        assertEquals("GB", locationAverage.getData().getColumn("Country").get(0));
        assertEquals(3, locationAverage.getData().getColumn("TransactionCount").get(0));
        assertEquals(50.0, locationAverage.getData().getColumn("Percentage").get(0));

        AggregationResult amountAggregation = (AggregationResult) transactionMap.get("AmountAggregation");
        assertEquals(1050.0, amountAggregation.getData().getColumn("Total").get(0));
        assertEquals(175.0, amountAggregation.getData().getColumn("Average").get(0));
        assertEquals(50.0, amountAggregation.getData().getColumn("Min").get(0));
        assertEquals(300.0, amountAggregation.getData().getColumn("Max").get(0));

        InformativeResult topBeneficiaries = (InformativeResult) transactionMap.get("TopBeneficiaries");
        JsonNode topBeneficiariesJson = MAPPER.readTree(topBeneficiaries.getJson());
        assertEquals(3, topBeneficiariesJson.size());
        assertEquals("Alice Smith", topBeneficiariesJson.get(0).get("beneficiary").asText());
        assertEquals(3, topBeneficiariesJson.get(0).get("transactionCount").asInt());
        assertEquals("Bob Jones", topBeneficiariesJson.get(1).get("beneficiary").asText());
        assertEquals(2, topBeneficiariesJson.get(1).get("transactionCount").asInt());

        SuspiciousActivityEngineResult result = new SuspiciousActivityEngine(
                incomingTransaction,
                customerProfile,
                transactionMap
        ).addModule(new ClassificationModule()).run();

        assertEquals(1, result.size());
        assertEquals("Classification", result.all().get(0).get("Module Name"));
        assertEquals(true, result.all().get(0).get("Module Ran"));
        assertEquals(67, result.all().get(0).get("Risk Score"));
        assertEquals(
                "Beneficiary country 'US' seen in 2 of 3 past transactions (score: 67/100).",
                result.all().get(0).get("Comments")
        );

        JsonNode engineJson = MAPPER.readTree(result.toJson());
        assertEquals(1, engineJson.size());
        assertEquals(67, engineJson.get(0).get("Risk Score").asInt());
        assertEquals("Classification", engineJson.get(0).get("Module Name").asText());
    }

    @Test
    void personalCustomerJourney_withUnseenBeneficiaryCountry_returnsHighestRiskAssessment() {
        DataFrame customerProfile = validPersonalCustomerProfile();
        DataFrame transactionHistory = validTransactionHistory();
        DataFrame incomingTransaction = validIncomingTransaction("NG");

        assertFalse(new PersonalCustomerValidator().validate(customerProfile).hasErrors());
        assertFalse(new TransactionValidator().validate(transactionHistory).hasErrors());
        assertFalse(new TransactionValidator().validate(incomingTransaction).hasErrors());

        HashMap<String, Object> transactionMap = new TransactionMap(transactionHistory)
                .addModule(new CountryFrequencyModule())
                .run();

        SuspiciousActivityEngineResult result = new SuspiciousActivityEngine(
                incomingTransaction,
                customerProfile,
                transactionMap
        ).addModule(new ClassificationModule()).run();

        assertEquals(1, result.size());
        assertEquals(0, result.all().get(0).get("Risk Score"));
        assertEquals(
                "Beneficiary country 'NG' has never appeared in customer's transaction history.",
                result.all().get(0).get("Comments")
        );
    }

    @Test
    void invalidIncomingTransaction_isRejectedByValidationBeforeScoring() {
        DataFrame invalidTransaction = DataFrame.byColumn(
                "customernumber",
                "accountnumber",
                "date",
                "time",
                "beneficiaryname",
                "beneficiaryaccountnumber",
                "beneficiarysortcode",
                "beneficiaryreference",
                "purposeofpayment",
                "countryoforigin",
                "countryofbeneficiary",
                "amount"
        ).of(
                Series.of(1042),
                Series.of(1234567),
                Series.of(LocalDate.of(2999, 1, 1)),
                Series.of(LocalTime.of(9, 0)),
                Series.of("Risky Recipient"),
                Series.of(99887766),
                Series.of(12345),
                Series.of("INVALID"),
                Series.of("Transfer"),
                Series.of("GB"),
                Series.of("ZZ"),
                Series.of(999.99)
        );

        ValidationReport report = new TransactionValidator().validate(invalidTransaction);

        assertTrue(report.hasErrors());
        assertTrue(hasIssueFor(report, "accountnumber", "must be exactly 8 digits"));
        assertTrue(hasIssueFor(report, "date", "is in the future"));
        assertTrue(hasIssueFor(report, "beneficiarysortcode", "must be exactly 6 digits"));
        assertTrue(hasIssueFor(report, "countryofbeneficiary", "is not a valid ISO 3166-1 alpha-2 country code"));
    }

    private static boolean hasIssueFor(ValidationReport report, String column, String descriptionFragment) {
        return report.getIssuesByColumn(column).stream()
                .map(ValidationIssue::getDescription)
                .anyMatch(description -> description.contains(descriptionFragment));
    }

    private static DataFrame validPersonalCustomerProfile() {
        return DataFrame.byColumn(
                "firstname",
                "middlename",
                "lastname",
                "Date Of Birth",
                "Address",
                "Nationality",
                "Country Of Birth",
                "Residence",
                "Cash Turnover",
                "Countries of Outward Payments",
                "Countries of Inward Payments"
        ).of(
                Series.of("Daniel"),
                Series.of("Jose"),
                Series.of("Ruano"),
                Series.of(LocalDate.of(1990, 6, 15)),
                Series.of("10 Downing Street, London SW1A 2AA"),
                Series.of("GB"),
                Series.of("GB"),
                Series.of("GB"),
                Series.of("10001-50000"),
                Series.of((Object) new String[]{"GB", "US", "FR"}),
                Series.of((Object) new String[]{"GB", "US"})
        );
    }

    private static DataFrame validTransactionHistory() {
        return DataFrame.byColumn(
                "customernumber",
                "accountnumber",
                "date",
                "time",
                "beneficiaryname",
                "beneficiaryaccountnumber",
                "beneficiarysortcode",
                "beneficiaryreference",
                "purposeofpayment",
                "countryoforigin",
                "countryofbeneficiary",
                "amount"
        ).of(
                Series.of(1042, 1042, 1042, 1042, 1042, 1042),
                Series.of(12345678, 12345678, 12345678, 12345678, 12345678, 12345678),
                Series.of(
                        LocalDate.of(2024, 1, 10),
                        LocalDate.of(2024, 1, 11),
                        LocalDate.of(2024, 1, 12),
                        LocalDate.of(2024, 1, 13),
                        LocalDate.of(2024, 1, 14),
                        LocalDate.of(2024, 1, 15)
                ),
                Series.of(
                        LocalTime.of(9, 15),
                        LocalTime.of(10, 30),
                        LocalTime.of(11, 45),
                        LocalTime.of(12, 5),
                        LocalTime.of(13, 20),
                        LocalTime.of(14, 35)
                ),
                Series.of("Alice Smith", "Bob Jones", "Alice Smith", "Alice Smith", "Carla Diaz", "Bob Jones"),
                Series.of(22334455, 33445566, 22334455, 22334455, 44556677, 33445566),
                Series.of(112233, 223344, 112233, 112233, 334455, 223344),
                Series.of("Rent", "Invoice", "Utilities", "Travel", "Hotel", "Supplies"),
                Series.of("Bills", "Services", "Bills", "Travel", "Accommodation", "Services"),
                Series.of("GB", "GB", "US", "GB", "FR", "US"),
                Series.of("GB", "US", "US", "GB", "FR", "US"),
                Series.of(100.0, 200.0, 150.0, 250.0, 300.0, 50.0)
        );
    }

    private static DataFrame validIncomingTransaction(String beneficiaryCountry) {
        return DataFrame.byColumn(
                "customernumber",
                "accountnumber",
                "date",
                "time",
                "beneficiaryname",
                "beneficiaryaccountnumber",
                "beneficiarysortcode",
                "beneficiaryreference",
                "purposeofpayment",
                "countryoforigin",
                "countryofbeneficiary",
                "amount"
        ).of(
                Series.of(1042),
                Series.of(12345678),
                Series.of(LocalDate.of(2024, 2, 1)),
                Series.of(LocalTime.of(12, 30)),
                Series.of("Vendor Payment"),
                Series.of(77889911),
                Series.of(654321),
                Series.of("FEB-INVOICE"),
                Series.of("Services"),
                Series.of("GB"),
                Series.of(beneficiaryCountry),
                Series.of(175.0)
        );
    }
}
