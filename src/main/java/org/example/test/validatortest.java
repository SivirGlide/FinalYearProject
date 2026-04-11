package org.example.test;

import org.dflib.DataFrame;
import org.dflib.Series;
import org.example.lib.validator.PersonalCustomerValidator;
import org.example.lib.validator.ValidationReport;

import java.time.LocalDate;

public class validatortest {

    public static void main(String[] args) {

        DataFrame df = DataFrame.byColumn(
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
                Series.of("Alice",   "Bob",  "Charlie"),
                Series.of("Marie",   null,   "James"),      // null middlename — allowed
                Series.of("Smith",   "Jones", null),        // null lastname — NOT allowed → warning
                Series.of(LocalDate.of(1990, 5, 12),
                        LocalDate.of(2015, 3, 1),        // Under 18 → outlier warning
                        LocalDate.of(1885, 1, 1)),       // Over 120 → outlier warning
                Series.of("123 High St", "45 Low Rd", "78 Main Ave"),
                Series.of("GB", "USA", "DE"),               // "USA" is 3 chars → warning
                Series.of("GB", "US", "DE"),
                Series.of("GB", "US", "DE"),
                Series.of("0-10000", "UNKNOWN_VALUE", "50001-100000"), // "UNKNOWN_VALUE" not in dropdown
                Series.of(new String[]{"US","DE"}, new String[]{"GB"}, new String[]{"FRANCE"}), // "FRANCE" not 2-char
                Series.of(new String[]{"US"}, new String[]{"DE","GB"}, new String[]{"US"})
        );

        ValidationReport report = PersonalCustomerValidator.validate(df);

        // ── 3. Use the report ─────────────────────────────────────────────────

        // Option A: print formatted summary to console
        report.printSummary();

        // Option B: query results programmatically
        if (report.hasErrors()) {
            System.out.println("\nValidation FAILED — errors must be resolved before processing.");
        } else if (report.hasWarnings()) {
            System.out.println("\nValidation PASSED with warnings — review warnings before proceeding.");
        } else {
            System.out.println("\nValidation PASSED — DataFrame looks clean.");
        }

        // Option C: inspect issues for a specific column only
        report.getIssuesByColumn("Nationality").forEach(System.out::println);
    }
}
