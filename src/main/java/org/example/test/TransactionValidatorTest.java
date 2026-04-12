package org.example.test;

import org.dflib.DataFrame;
import org.dflib.Series;
import org.example.lib.validator.TransactionValidator;
import org.example.lib.validator.ValidationIssue;
import org.example.lib.validator.ValidationReport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for TransactionValidator.
 *
 * HOW JUNIT 5 WORKS (quick primer):
 *   @Test         — marks a method as a test case. JUnit will run it automatically.
 *   @Nested       — lets you group related tests into inner classes so the output
 *                   is organised by category rather than one flat list.
 *   @DisplayName  — gives the test a readable label in the test report instead of
 *                   the raw method name.
 *   assertEquals  — asserts that two values are equal; fails the test if not.
 *   assertTrue    — asserts a condition is true; fails the test if not.
 *   assertFalse   — asserts a condition is false; fails the test if not.
 *
 * TEST STRUCTURE:
 *   Each @Nested class covers one category of validation:
 *     - ValidData          → a clean DataFrame should produce no errors or warnings
 *     - MissingColumns     → absent required columns should produce ERRORs
 *     - TypeMismatches     → wrong Java types should produce ERRORs
 *     - NullChecks         → nulls in non-nullable columns should produce WARNINGs
 *     - IsoCodeLength      → country codes that aren't 2 chars should produce WARNINGs
 *     - CrossColumnChecks  → same origin/beneficiary country should produce INFO
 *     - UnexpectedColumns  → columns not in the schema should produce INFO
 */
@DisplayName("TransactionValidator")
class TransactionValidatorTest {

    // =========================================================================
    // Shared helper — builds a valid single-row DataFrame that passes all checks.
    // Individual tests start from this and deliberately break one thing at a time.
    // This avoids repeating the full column list in every single test.
    // =========================================================================

    /**
     * Returns a DataFrame containing one perfectly valid transaction row.
     * Every test that needs to introduce a fault starts by pulling these
     * series out, replacing the faulty one, and rebuilding the DataFrame.
     */
    private DataFrame validDataFrame() {
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
                "countryofbeneficiary"
        ).of(
                Series.of(1001, 1002),
                Series.of(20001, 20002),
                Series.of(LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 16)),
                Series.of(LocalTime.of(9, 30), LocalTime.of(14, 0)),
                Series.of("Jane Smith", "Bob Jones"),
                Series.of(30001, 30002),
                Series.of(400001, 400002),
                Series.of("Invoice 99", null),       // null allowed for beneficiaryreference
                Series.of("Salary", "Goods"),
                Series.of("GB", "US"),
                Series.of("DE", "FR")
        );
    }

    // =========================================================================
    // 1. Valid data
    // =========================================================================

    @Nested
    @DisplayName("Valid DataFrame")
    class ValidData {

        @Test
        @DisplayName("Should produce no errors or warnings when all data is correct")
        void shouldPassCleanDataFrame() {
            ValidationReport report = TransactionValidator.validate(validDataFrame());

            assertFalse(report.hasErrors(),
                    "A fully valid DataFrame should produce no errors");
            assertFalse(report.hasWarnings(),
                    "A fully valid DataFrame should produce no warnings");
        }

        @Test
        @DisplayName("Should allow null beneficiaryreference as it is marked nullable")
        void shouldAllowNullBeneficiaryReference() {
            ValidationReport report = TransactionValidator.validate(validDataFrame());

            List<ValidationIssue> referenceIssues = report.getIssuesByColumn("beneficiaryreference");
            assertTrue(referenceIssues.isEmpty(),
                    "Null beneficiaryreference should not produce any issues");
        }
    }

    // =========================================================================
    // 2. Missing columns
    // =========================================================================

    @Nested
    @DisplayName("Missing Columns")
    class MissingColumns {

        @Test
        @DisplayName("Should error when customernumber is missing")
        void shouldErrorOnMissingCustomerNumber() {
            DataFrame df = buildWithout("customernumber");
            ValidationReport report = TransactionValidator.validate(df);

            assertTrue(report.hasErrors());
            assertColumnHasSeverity(report, "customernumber", ValidationIssue.Severity.ERROR);
        }

        @Test
        @DisplayName("Should error when date is missing")
        void shouldErrorOnMissingDate() {
            DataFrame df = buildWithout("date");
            ValidationReport report = TransactionValidator.validate(df);

            assertTrue(report.hasErrors());
            assertColumnHasSeverity(report, "date", ValidationIssue.Severity.ERROR);
        }

        @Test
        @DisplayName("Should error when time is missing")
        void shouldErrorOnMissingTime() {
            DataFrame df = buildWithout("time");
            ValidationReport report = TransactionValidator.validate(df);

            assertTrue(report.hasErrors());
            assertColumnHasSeverity(report, "time", ValidationIssue.Severity.ERROR);
        }

        @Test
        @DisplayName("Should error when countryoforigin is missing")
        void shouldErrorOnMissingCountryOfOrigin() {
            DataFrame df = buildWithout("countryoforigin");
            ValidationReport report = TransactionValidator.validate(df);

            assertTrue(report.hasErrors());
            assertColumnHasSeverity(report, "countryoforigin", ValidationIssue.Severity.ERROR);
        }

        @Test
        @DisplayName("Should error when countryofbeneficiary is missing")
        void shouldErrorOnMissingCountryOfBeneficiary() {
            DataFrame df = buildWithout("countryofbeneficiary");
            ValidationReport report = TransactionValidator.validate(df);

            assertTrue(report.hasErrors());
            assertColumnHasSeverity(report, "countryofbeneficiary", ValidationIssue.Severity.ERROR);
        }

        @Test
        @DisplayName("Should report one error per missing column when multiple are absent")
        void shouldReportMultipleErrorsForMultipleMissingColumns() {
            // Build a DataFrame with only two columns present
            DataFrame df = DataFrame.byColumn("customernumber", "accountnumber")
                    .of(Series.of(1001), Series.of(20001));

            ValidationReport report = TransactionValidator.validate(df);

            long errorCount = report.getIssues(ValidationIssue.Severity.ERROR).size();
            // 9 columns missing (11 total - 2 present)
            assertEquals(9, errorCount,
                    "Should produce exactly one ERROR per missing required column");
        }

        // ── Helper: build a valid DataFrame with one column deliberately removed ──
        private DataFrame buildWithout(String excludedColumn) {
            DataFrame full = validDataFrame();

            // Index implements Iterable<String> but has no .stream() in DFLib 1.3.0
            // so we collect the column names we want to keep using a plain for-loop.
            List<String> remaining = new ArrayList<>();
            for (String col : full.getColumnsIndex()) {
                if (!col.equals(excludedColumn)) {
                    remaining.add(col);
                }
            }

            // cols(String...).select() is the DFLib 1.3.0 way to project a subset of columns
            return full.cols(remaining.toArray(new String[0])).select();
        }
    }

    // =========================================================================
    // 3. Type mismatches
    // =========================================================================

    @Nested
    @DisplayName("Type Mismatches")
    class TypeMismatches {

        @Test
        @DisplayName("Should error when customernumber contains Strings instead of Integers")
        void shouldErrorWhenCustomerNumberIsWrongType() {
            DataFrame df = DataFrame.byColumn(
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
                    "countryofbeneficiary"
            ).of(
                    Series.of("NOT_AN_INT"),          // ← wrong type: String instead of Int
                    Series.of(20001),
                    Series.of(LocalDate.of(2024, 1, 15)),
                    Series.of(LocalTime.of(9, 30)),
                    Series.of("Jane Smith"),
                    Series.of(30001),
                    Series.of(400001),
                    Series.of("Invoice 99"),
                    Series.of("Salary"),
                    Series.of("GB"),
                    Series.of("DE")
            );

            ValidationReport report = TransactionValidator.validate(df);

            assertTrue(report.hasErrors());
            assertColumnHasSeverity(report, "customernumber", ValidationIssue.Severity.ERROR);
        }

        @Test
        @DisplayName("Should error when date contains Strings instead of LocalDate")
        void shouldErrorWhenDateIsWrongType() {
            DataFrame df = DataFrame.byColumn(
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
                    "countryofbeneficiary"
            ).of(
                    Series.of(1001),
                    Series.of(20001),
                    Series.of("2024-01-15"),          // ← wrong type: String instead of LocalDate
                    Series.of(LocalTime.of(9, 30)),
                    Series.of("Jane Smith"),
                    Series.of(30001),
                    Series.of(400001),
                    Series.of("Invoice 99"),
                    Series.of("Salary"),
                    Series.of("GB"),
                    Series.of("DE")
            );

            ValidationReport report = TransactionValidator.validate(df);

            assertTrue(report.hasErrors());
            assertColumnHasSeverity(report, "date", ValidationIssue.Severity.ERROR);
        }

        @Test
        @DisplayName("Should error when time contains Strings instead of LocalTime")
        void shouldErrorWhenTimeIsWrongType() {
            DataFrame df = DataFrame.byColumn(
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
                    "countryofbeneficiary"
            ).of(
                    Series.of(1001),
                    Series.of(20001),
                    Series.of(LocalDate.of(2024, 1, 15)),
                    Series.of("09:30"),               // ← wrong type: String instead of LocalTime
                    Series.of("Jane Smith"),
                    Series.of(30001),
                    Series.of(400001),
                    Series.of("Invoice 99"),
                    Series.of("Salary"),
                    Series.of("GB"),
                    Series.of("DE")
            );

            ValidationReport report = TransactionValidator.validate(df);

            assertTrue(report.hasErrors());
            assertColumnHasSeverity(report, "time", ValidationIssue.Severity.ERROR);
        }
    }

    // =========================================================================
    // 4. Null checks
    // =========================================================================

    @Nested
    @DisplayName("Null Checks")
    class NullChecks {

        @Test
        @DisplayName("Should warn when customernumber contains a null")
        void shouldWarnOnNullCustomerNumber() {
            DataFrame df = DataFrame.byColumn(
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
                    "countryofbeneficiary"
            ).of(
                    Series.of(1001, null, 1003),      // ← null in non-nullable column
                    Series.of(20001, 20002, 20003),
                    Series.of(LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 16), LocalDate.of(2024, 1, 17)),
                    Series.of(LocalTime.of(9, 30), LocalTime.of(10, 0), LocalTime.of(11, 0)),
                    Series.of("Jane Smith", "Bob Jones", "Alice Brown"),
                    Series.of(30001, 30002, 30003),
                    Series.of(400001, 400002, 400003),
                    Series.of("Inv 1", "Inv 2", "Inv 3"),
                    Series.of("Salary", "Goods", "Rent"),
                    Series.of("GB", "US", "DE"),
                    Series.of("DE", "FR", "US")
            );

            ValidationReport report = TransactionValidator.validate(df);

            assertColumnHasSeverity(report, "customernumber", ValidationIssue.Severity.WARNING);
        }

        @Test
        @DisplayName("Should warn when purposeofpayment contains a null")
        void shouldWarnOnNullPurposeOfPayment() {
            DataFrame df = DataFrame.byColumn(
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
                    "countryofbeneficiary"
            ).of(
                    Series.of(1001),
                    Series.of(20001),
                    Series.of(LocalDate.of(2024, 1, 15)),
                    Series.of(LocalTime.of(9, 30)),
                    Series.of("Jane Smith"),
                    Series.of(30001),
                    Series.of(400001),
                    Series.of("Invoice 99"),
                    Series.of((Object) null),         // ← null in non-nullable column
                    Series.of("GB"),
                    Series.of("DE")
            );

            ValidationReport report = TransactionValidator.validate(df);

            assertColumnHasSeverity(report, "purposeofpayment", ValidationIssue.Severity.WARNING);
        }

        @Test
        @DisplayName("Should NOT warn when beneficiaryreference is null as it is nullable")
        void shouldNotWarnOnNullBeneficiaryReference() {
            // beneficiaryreference is the one nullable column — no warning expected
            ValidationReport report = TransactionValidator.validate(validDataFrame());

            List<ValidationIssue> issues = report.getIssuesByColumn("beneficiaryreference");
            assertTrue(issues.isEmpty(),
                    "Null beneficiaryreference should never produce a warning");
        }
    }

    // =========================================================================
    // 5. ISO code length checks
    // =========================================================================

    @Nested
    @DisplayName("ISO Code Length")
    class IsoCodeLength {

        @Test
        @DisplayName("Should warn when countryoforigin is not 2 characters")
        void shouldWarnOnInvalidCountryOfOriginLength() {
            DataFrame df = DataFrame.byColumn(
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
                    "countryofbeneficiary"
            ).of(
                    Series.of(1001),
                    Series.of(20001),
                    Series.of(LocalDate.of(2024, 1, 15)),
                    Series.of(LocalTime.of(9, 30)),
                    Series.of("Jane Smith"),
                    Series.of(30001),
                    Series.of(400001),
                    Series.of("Invoice 99"),
                    Series.of("Salary"),
                    Series.of("GBR"),                 // ← 3 chars, should be 2
                    Series.of("DE")
            );

            ValidationReport report = TransactionValidator.validate(df);

            assertColumnHasSeverity(report, "countryoforigin", ValidationIssue.Severity.WARNING);
        }

        @Test
        @DisplayName("Should warn when countryofbeneficiary is not 2 characters")
        void shouldWarnOnInvalidCountryOfBeneficiaryLength() {
            DataFrame df = DataFrame.byColumn(
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
                    "countryofbeneficiary"
            ).of(
                    Series.of(1001),
                    Series.of(20001),
                    Series.of(LocalDate.of(2024, 1, 15)),
                    Series.of(LocalTime.of(9, 30)),
                    Series.of("Jane Smith"),
                    Series.of(30001),
                    Series.of(400001),
                    Series.of("Invoice 99"),
                    Series.of("Salary"),
                    Series.of("GB"),
                    Series.of("D")                    // ← 1 char, should be 2
            );

            ValidationReport report = TransactionValidator.validate(df);

            assertColumnHasSeverity(report, "countryofbeneficiary", ValidationIssue.Severity.WARNING);
        }

        @Test
        @DisplayName("Should not warn when both country codes are exactly 2 characters")
        void shouldNotWarnOnValidIsoCodes() {
            ValidationReport report = TransactionValidator.validate(validDataFrame());

            assertTrue(report.getIssuesByColumn("countryoforigin").isEmpty());
            assertTrue(report.getIssuesByColumn("countryofbeneficiary").isEmpty());
        }
    }

    // =========================================================================
    // 6. Cross-column checks
    // =========================================================================

    @Nested
    @DisplayName("Cross-Column Checks")
    class CrossColumnChecks {

        @Test
        @DisplayName("Should produce INFO when countryoforigin equals countryofbeneficiary")
        void shouldFlagDomesticTransaction() {
            DataFrame df = DataFrame.byColumn(
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
                    "countryofbeneficiary"
            ).of(
                    Series.of(1001),
                    Series.of(20001),
                    Series.of(LocalDate.of(2024, 1, 15)),
                    Series.of(LocalTime.of(9, 30)),
                    Series.of("Jane Smith"),
                    Series.of(30001),
                    Series.of(400001),
                    Series.of("Invoice 99"),
                    Series.of("Salary"),
                    Series.of("GB"),
                    Series.of("GB")                   // ← same as countryoforigin → domestic
            );

            ValidationReport report = TransactionValidator.validate(df);
            List<ValidationIssue> infoIssues = report.getIssues(ValidationIssue.Severity.INFO);

            assertFalse(infoIssues.isEmpty(),
                    "A domestic transaction should produce at least one INFO issue");
        }

        @Test
        @DisplayName("Should produce one INFO per domestic transaction row")
        void shouldFlagEachDomesticRowSeparately() {
            DataFrame df = DataFrame.byColumn(
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
                    "countryofbeneficiary"
            ).of(
                    Series.of(1001, 1002, 1003),
                    Series.of(20001, 20002, 20003),
                    Series.of(LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 16), LocalDate.of(2024, 1, 17)),
                    Series.of(LocalTime.of(9, 30), LocalTime.of(10, 0), LocalTime.of(11, 0)),
                    Series.of("Jane Smith", "Bob Jones", "Alice Brown"),
                    Series.of(30001, 30002, 30003),
                    Series.of(400001, 400002, 400003),
                    Series.of("Inv 1", "Inv 2", "Inv 3"),
                    Series.of("Salary", "Goods", "Rent"),
                    Series.of("GB", "US", "DE"),      // rows 0 and 2 are domestic
                    Series.of("GB", "FR", "DE")       // rows 0 and 2 match origin
            );

            ValidationReport report = TransactionValidator.validate(df);
            List<ValidationIssue> infoIssues = report.getIssues(ValidationIssue.Severity.INFO);

            assertEquals(2, infoIssues.size(),
                    "Should produce exactly one INFO for each domestic transaction row");
        }

        @Test
        @DisplayName("Should NOT flag international transactions")
        void shouldNotFlagInternationalTransaction() {
            // validDataFrame() has GB→DE and US→FR — both international
            ValidationReport report = TransactionValidator.validate(validDataFrame());

            List<ValidationIssue> infoIssues = report.getIssues(ValidationIssue.Severity.INFO);
            assertTrue(infoIssues.isEmpty(),
                    "International transactions should produce no INFO issues");
        }
    }

    // =========================================================================
    // 7. Unexpected columns
    // =========================================================================

    @Nested
    @DisplayName("Unexpected Columns")
    class UnexpectedColumns {

        @Test
        @DisplayName("Should produce INFO for columns not defined in the schema")
        void shouldFlagUnexpectedColumn() {
            // Build a DataFrame that includes all valid columns plus one extra
            // that the schema does not define. addColumn() does not exist in
            // DFLib 1.3.0 so we construct the full DataFrame explicitly.
            DataFrame df = DataFrame.byColumn(
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
                    "internalauditflag"           // ← not in schema
            ).of(
                    Series.of(1001, 1002),
                    Series.of(20001, 20002),
                    Series.of(LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 16)),
                    Series.of(LocalTime.of(9, 30), LocalTime.of(14, 0)),
                    Series.of("Jane Smith", "Bob Jones"),
                    Series.of(30001, 30002),
                    Series.of(400001, 400002),
                    Series.of("Invoice 99", null),
                    Series.of("Salary", "Goods"),
                    Series.of("GB", "US"),
                    Series.of("DE", "FR"),
                    Series.of("Y", "N")
            );

            ValidationReport report = TransactionValidator.validate(df);
            List<ValidationIssue> infoIssues = report.getIssues(ValidationIssue.Severity.INFO);

            assertFalse(infoIssues.isEmpty(),
                    "An unrecognised column should produce at least one INFO issue");
            assertColumnHasSeverity(report, "internalauditflag", ValidationIssue.Severity.INFO);
        }

        @Test
        @DisplayName("Should not error or warn on unexpected columns — only INFO")
        void unexpectedColumnShouldNotCauseErrorOrWarning() {
            DataFrame df = DataFrame.byColumn(
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
                    "internalauditflag"           // ← not in schema
            ).of(
                    Series.of(1001, 1002),
                    Series.of(20001, 20002),
                    Series.of(LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 16)),
                    Series.of(LocalTime.of(9, 30), LocalTime.of(14, 0)),
                    Series.of("Jane Smith", "Bob Jones"),
                    Series.of(30001, 30002),
                    Series.of(400001, 400002),
                    Series.of("Invoice 99", null),
                    Series.of("Salary", "Goods"),
                    Series.of("GB", "US"),
                    Series.of("DE", "FR"),
                    Series.of("Y", "N")
            );

            ValidationReport report = TransactionValidator.validate(df);

            assertFalse(report.hasErrors(),
                    "An unexpected column alone should not cause an ERROR");
            assertFalse(report.hasWarnings(),
                    "An unexpected column alone should not cause a WARNING");
        }
    }

    // =========================================================================
    // Shared assertion helper
    // =========================================================================

    /**
     * Asserts that the report contains at least one issue of the given severity
     * for the specified column name.
     *
     * Using a helper keeps the individual test assertions concise and means
     * if the assertion logic ever needs changing, you change it in one place.
     */
    private void assertColumnHasSeverity(ValidationReport report,
                                         String columnName,
                                         ValidationIssue.Severity expectedSeverity) {
        boolean found = report.getIssuesByColumn(columnName)
                .stream()
                .anyMatch(i -> i.getSeverity() == expectedSeverity);

        assertTrue(found, String.format(
                "Expected at least one %s issue for column '%s' but none was found.%n" +
                        "All issues for that column: %s",
                expectedSeverity, columnName, report.getIssuesByColumn(columnName)
        ));
    }
}