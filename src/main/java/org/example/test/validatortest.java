package org.example.test;

import org.dflib.DataFrame;
import org.dflib.Series;
import org.example.lib.validator.PersonalCustomerValidator;
import org.example.lib.validator.ValidationReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PersonalCustomerValidatorTest {

    // ─────────────────────────────────────────────────────────────
    // Helper method — builds a single-row DataFrame with full control
    // over every field so each test is self-contained and readable.
    // ─────────────────────────────────────────────────────────────
    private DataFrame buildRow(
            String firstname,
            String middlename,
            String lastname,
            LocalDate dob,
            String address,
            String nationality,
            String countryOfBirth,
            String residence,
            String cashTurnover,
            String[] outwardPayments,
            String[] inwardPayments
    ) {
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
                Series.of(firstname),
                Series.of(middlename),
                Series.of(lastname),
                Series.of(dob),
                Series.of(address),
                Series.of(nationality),
                Series.of(countryOfBirth),
                Series.of(residence),
                Series.of(cashTurnover),
                Series.of(new String[][]{outwardPayments}),
                Series.of(new String[][]{inwardPayments})
        );
    }

    // Convenience: a fully valid single row with no issues
    private DataFrame validRow() {
        return buildRow(
                "Alice", "Marie", "Smith",
                LocalDate.of(1995, 5, 12),
                "123 High St",
                "GB", "GB", "GB",
                "0-10000",
                new String[]{"US", "DE"},
                new String[]{"US"}
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // 1. HAPPY PATH
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Fully valid row → no errors and no warnings")
    void fullyValidRow_noIssues() {
        ValidationReport report = PersonalCustomerValidator.validate(validRow());

        assertFalse(report.hasErrors(),   "Expected no errors for a valid row");
        assertFalse(report.hasWarnings(), "Expected no warnings for a valid row");
    }

    // ═══════════════════════════════════════════════════════════════
    // 2. REQUIRED FIELDS
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Null lastname → produces a warning or error")
    void nullLastname_producesIssue() {
        DataFrame df = buildRow(
                "Alice", "Marie", null,          // <── null lastname
                LocalDate.of(1990, 5, 12),
                "123 High St",
                "GB", "GB", "GB",
                "0-10000",
                new String[]{"US"}, new String[]{"US"}
        );

        ValidationReport report = PersonalCustomerValidator.validate(df);

        // The sample code comments say null lastname is "NOT allowed → warning"
        assertTrue(report.hasWarnings() || report.hasErrors(),
                "Null lastname should flag an issue");
    }

    @Test
    @DisplayName("Null middlename → no issue (middlename is optional)")
    void nullMiddlename_noIssue() {
        DataFrame df = buildRow(
                "Alice", null, "Smith",           // <── null middlename — allowed
                LocalDate.of(1990, 5, 12),
                "123 High St",
                "GB", "GB", "GB",
                "0-10000",
                new String[]{"US"}, new String[]{"US"}
        );

        ValidationReport report = PersonalCustomerValidator.validate(df);

        assertFalse(report.hasErrors(),   "Null middlename should not cause an error");
        assertFalse(report.hasWarnings(), "Null middlename should not cause a warning");
    }

    // ═══════════════════════════════════════════════════════════════
    // 3. DATE OF BIRTH EDGE CASES
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("DOB that makes customer under 18 → outlier warning")
    void underageDob_producesWarning() {
        // A birth date 10 years ago → customer is 10 years old
        LocalDate underageDob = LocalDate.now().minusYears(10);

        DataFrame df = buildRow(
                "Bob", null, "Jones",
                underageDob,                      // <── under 18
                "45 Low Rd",
                "GB", "GB", "GB",
                "0-10000",
                new String[]{"US"}, new String[]{"US"}
        );

        ValidationReport report = PersonalCustomerValidator.validate(df);

        assertTrue(report.hasWarnings() || report.hasErrors(),
                "Under-18 DOB should produce an outlier warning");
    }

    @Test
    @DisplayName("DOB over 120 years ago → outlier warning")
    void over120YearsDob_producesWarning() {
        DataFrame df = buildRow(
                "Charlie", "James", "Brown",
                LocalDate.of(1885, 1, 1),         // <── over 120 years ago
                "78 Main Ave",
                "GB", "GB", "GB",
                "0-10000",
                new String[]{"US"}, new String[]{"US"}
        );

        ValidationReport report = PersonalCustomerValidator.validate(df);

        assertTrue(report.hasWarnings() || report.hasErrors(),
                "DOB over 120 years ago should produce an outlier warning");
    }

    @Test
    @DisplayName("DOB exactly on the 18-year boundary → no age warning")
    void exactlyEighteenDob_noAgeWarning() {
        LocalDate eighteenToday = LocalDate.now().minusYears(18);

        DataFrame df = buildRow(
                "Alice", null, "Smith",
                eighteenToday,
                "123 High St",
                "GB", "GB", "GB",
                "0-10000",
                new String[]{"US"}, new String[]{"US"}
        );

        ValidationReport report = PersonalCustomerValidator.validate(df);

        // Boundary: exactly 18 should be valid
        List<?> dobIssues = report.getIssuesByColumn("Date Of Birth");
        assertTrue(dobIssues.isEmpty(),
                "Customer exactly 18 years old should not trigger an age warning");
    }

    // ═══════════════════════════════════════════════════════════════
    // 4. COUNTRY CODE FORMAT (must be exactly 2 characters)
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("3-char nationality code 'USA' → warning")
    void threeCharNationality_producesWarning() {
        DataFrame df = buildRow(
                "Alice", null, "Smith",
                LocalDate.of(1990, 5, 12),
                "123 High St",
                "USA",                            // <── 3 chars, should be "US"
                "GB", "GB",
                "0-10000",
                new String[]{"US"}, new String[]{"US"}
        );

        ValidationReport report = PersonalCustomerValidator.validate(df);

        assertTrue(report.hasWarnings() || report.hasErrors(),
                "3-char nationality code should produce a warning");

        // Also verify the issue is reported under the correct column
        assertFalse(report.getIssuesByColumn("Nationality").isEmpty(),
                "Issue should be attributed to the 'Nationality' column");
    }

    @Test
    @DisplayName("Valid 2-char nationality code 'DE' → no nationality issue")
    void twoCharNationality_noIssue() {
        DataFrame df = buildRow(
                "Alice", null, "Smith",
                LocalDate.of(1990, 5, 12),
                "123 High St",
                "DE",                             // <── correct 2-char code
                "GB", "GB",
                "0-10000",
                new String[]{"US"}, new String[]{"US"}
        );

        ValidationReport report = PersonalCustomerValidator.validate(df);

        assertTrue(report.getIssuesByColumn("Nationality").isEmpty(),
                "Valid 2-char code should not raise a Nationality issue");
    }

    // ═══════════════════════════════════════════════════════════════
    // 5. CASH TURNOVER DROPDOWN
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Unknown cash turnover value → warning or error")
    void unknownCashTurnover_producesIssue() {
        DataFrame df = buildRow(
                "Alice", null, "Smith",
                LocalDate.of(1990, 5, 12),
                "123 High St",
                "GB", "GB", "GB",
                "UNKNOWN_VALUE",                  // <── not a valid dropdown value
                new String[]{"US"}, new String[]{"US"}
        );

        ValidationReport report = PersonalCustomerValidator.validate(df);

        assertTrue(report.hasWarnings() || report.hasErrors(),
                "An unrecognised cash turnover value should raise an issue");

        assertFalse(report.getIssuesByColumn("Cash Turnover").isEmpty(),
                "Issue should be attributed to the 'Cash Turnover' column");
    }

    @Test
    @DisplayName("Valid cash turnover value '0-10000' → no issue")
    void validCashTurnover_noIssue() {
        ValidationReport report = PersonalCustomerValidator.validate(validRow());

        assertTrue(report.getIssuesByColumn("Cash Turnover").isEmpty(),
                "Valid cash turnover should not produce any issue");
    }

    // ═══════════════════════════════════════════════════════════════
    // 6. PAYMENT COUNTRY ARRAYS (each entry must be a 2-char code)
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("'FRANCE' in outward payments array → warning (not a 2-char code)")
    void longCountryNameInOutwardPayments_producesWarning() {
        DataFrame df = buildRow(
                "Alice", null, "Smith",
                LocalDate.of(1990, 5, 12),
                "123 High St",
                "GB", "GB", "GB",
                "0-10000",
                new String[]{"FRANCE"},           // <── should be "FR"
                new String[]{"US"}
        );

        ValidationReport report = PersonalCustomerValidator.validate(df);

        assertTrue(report.hasWarnings() || report.hasErrors(),
                "'FRANCE' in outward payments should produce an issue");

        assertFalse(
                report.getIssuesByColumn("Countries of Outward Payments").isEmpty(),
                "Issue should be attributed to 'Countries of Outward Payments'"
        );
    }

    @Test
    @DisplayName("Valid 2-char codes in all payment arrays → no payment issues")
    void validPaymentCodes_noIssue() {
        ValidationReport report = PersonalCustomerValidator.validate(validRow());

        assertTrue(report.getIssuesByColumn("Countries of Outward Payments").isEmpty(),
                "Valid outward payment codes should raise no issue");
        assertTrue(report.getIssuesByColumn("Countries of Inward Payments").isEmpty(),
                "Valid inward payment codes should raise no issue");
    }

    // ═══════════════════════════════════════════════════════════════
    // 7. MULTI-ROW MIXED DATA (mirrors the original main() scenario)
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Mixed DataFrame with known bad rows → report has warnings")
    void mixedDataFrame_hasWarnings() {
        // This is essentially the same dataset from main() so we know it
        // must produce warnings based on the comments in the original code.
        DataFrame df = DataFrame.byColumn(
                "firstname", "middlename", "lastname",
                "Date Of Birth", "Address",
                "Nationality", "Country Of Birth", "Residence",
                "Cash Turnover",
                "Countries of Outward Payments",
                "Countries of Inward Payments"
        ).of(
                Series.of("Alice",   "Bob",     "Charlie"),
                Series.of("Marie",   null,      "James"),
                Series.of("Smith",   "Jones",   null),              // null lastname
                Series.of(
                        LocalDate.of(1990, 5, 12),
                        LocalDate.of(2015, 3, 1),                  // under 18
                        LocalDate.of(1885, 1, 1)                   // over 120
                ),
                Series.of("123 High St", "45 Low Rd", "78 Main Ave"),
                Series.of("GB",  "USA", "DE"),                     // "USA" = 3 chars
                Series.of("GB",  "US",  "DE"),
                Series.of("GB",  "US",  "DE"),
                Series.of("0-10000", "UNKNOWN_VALUE", "50001-100000"),
                Series.of(new String[]{"US","DE"}, new String[]{"GB"}, new String[]{"FRANCE"}),
                Series.of(new String[]{"US"},       new String[]{"DE","GB"}, new String[]{"US"})
        );

        ValidationReport report = PersonalCustomerValidator.validate(df);

        assertTrue(report.hasWarnings(), "Mixed data should produce at least one warning");
    }

    @Test
    @DisplayName("getIssuesByColumn returns non-null list for any column name")
    void getIssuesByColumn_neverReturnsNull() {
        ValidationReport report = PersonalCustomerValidator.validate(validRow());

        // Should return an empty list, not null, for a column with no issues
        assertNotNull(report.getIssuesByColumn("Nationality"),
                "getIssuesByColumn should return an empty list, not null");

        // Should also handle a completely unknown column gracefully
        assertNotNull(report.getIssuesByColumn("NonExistentColumn"),
                "getIssuesByColumn should not throw or return null for unknown columns");
    }
}
