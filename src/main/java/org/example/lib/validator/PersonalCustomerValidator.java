package org.example.lib.validator;

import org.dflib.DataFrame;
import org.dflib.Series;
import org.example.lib.common.countries.IsoCountryCodes;
import org.example.lib.common.schemas.PersonalCustomerSchema;
import org.example.lib.validator.report.ValidationIssue;
import org.example.lib.validator.report.ValidationReport;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

public class PersonalCustomerValidator implements DataValidator {

    public PersonalCustomerValidator() {}

    private static final PersonalCustomerSchema SCHEMA = new PersonalCustomerSchema();

    // UK postcodes follow a well-defined format, e.g. "SW1A 1AA" or "M1 1AE".
    // This pattern uses find() rather than matches(), so it will detect a valid
    // postcode embedded anywhere within the full address string.
    private static final Pattern UK_POSTCODE_PATTERN = Pattern.compile(
            "[A-Z]{1,2}[0-9][0-9A-Z]?\\s[0-9][A-Z]{2}",
            Pattern.CASE_INSENSITIVE
    );

    private static final int MIN_AGE = 1;
    private static final int MAX_AGE = 123;

    @Override
    public ValidationReport validate(DataFrame df) {
        ValidationReport report = new SchemaValidator(SCHEMA).validate(df);
        return this.CustomValidation(report, df);
    }

    /**
     * Personal-customer-specific checks appended on top of the generic schema report.
     * Each private method below addresses one business rule.
     */
    protected ValidationReport CustomValidation(ValidationReport report, DataFrame df) {
        checkDateOfBirth(report, df);
        checkUKAddresses(report, df);
        checkSingleCountryColumns(report, df);
        checkCountryArrayColumns(report, df);
        return report;
    }

    // -------------------------------------------------------------------------
    // Private checks
    // -------------------------------------------------------------------------

    /**
     * Checks that each customer's date of birth falls within a plausible age range.
     *
     * java.time.Period.between(dob, today).getYears() calculates the full
     * completed years between two dates — the same way you would work out
     * someone's age from their birthday.
     *
     *   Under 18  → ERROR   (must be an adult)
     *   Over 120  → WARNING (not impossible, but very likely a data-entry mistake)
     */
    private void checkDateOfBirth(ValidationReport report, DataFrame df) {
        Series<?> series = getSeries(df, "Date Of Birth");
        if (series == null) return;

        LocalDate today = LocalDate.now();

        for (int i = 0; i < series.size(); i++) {
            Object value = series.get(i);
            if (value == null) continue;
            if (!(value instanceof LocalDate dob)) continue; // type mismatch already caught by SchemaValidator

            int age = Period.between(dob, today).getYears();

            if (age < MIN_AGE) {
                report.addIssue(new ValidationIssue(
                        ValidationIssue.Severity.ERROR,
                        "Date Of Birth",
                        String.format("Customer age %d is below the minimum allowed age of %d.", age, MIN_AGE),
                        i
                ));
            } else if (age > MAX_AGE) {
                report.addIssue(new ValidationIssue(
                        ValidationIssue.Severity.WARNING,
                        "Date Of Birth",
                        String.format(
                                "Customer age %d exceeds the maximum plausible age of %d — possible data-entry error.",
                                age, MAX_AGE
                        ),
                        i
                ));
            }
        }
    }

    /**
     * Checks that each address contains a recognisable UK postcode.
     *
     * Pattern.find() scans the string for the postcode anywhere within it,
     * so a full address like "10 Downing Street, London, SW1A 2AA" passes
     * without needing to know exactly where in the string the postcode sits.
     *
     * This is a WARNING rather than an ERROR because the address field is a
     * free-text string — a missing or malformed postcode is suspicious but
     * does not make the record definitively unusable.
     */
    private void checkUKAddresses(ValidationReport report, DataFrame df) {
        Series<?> series = getSeries(df, "Address");
        if (series == null) return;

        for (int i = 0; i < series.size(); i++) {
            Object value = series.get(i);
            if (value == null) continue;

            String address = value.toString();
            if (!UK_POSTCODE_PATTERN.matcher(address).find()) {
                report.addIssue(new ValidationIssue(
                        ValidationIssue.Severity.WARNING,
                        "Address",
                        String.format("Address '%s' does not appear to contain a valid UK postcode.", address),
                        i
                ));
            }
        }
    }

    /**
     * Checks that the single-value country columns each hold a valid
     * ISO 3166-1 alpha-2 country code.
     *
     * These columns store exactly one country per row as a plain String,
     * so validation is a straightforward look-up against IsoCountryCodes.
     */
    private void checkSingleCountryColumns(ValidationReport report, DataFrame df) {
        String[] countryColumns = {"Nationality", "Country Of Birth", "Residence"};

        for (String colName : countryColumns) {
            Series<?> series = getSeries(df, colName);
            if (series == null) continue;

            for (int i = 0; i < series.size(); i++) {
                Object value = series.get(i);
                if (value == null) continue;

                if (!IsoCountryCodes.isValid(value.toString())) {
                    report.addIssue(new ValidationIssue(
                            ValidationIssue.Severity.ERROR,
                            colName,
                            String.format("'%s' is not a valid ISO 3166-1 alpha-2 country code.", value),
                            i
                    ));
                }
            }
        }
    }

    /**
     * Checks that every entry inside each country-list column is a valid
     * ISO 3166-1 alpha-2 country code.
     *
     * These columns (Countries of Outward/Inward Payments) hold a String[]
     * per row rather than a single String, so the outer loop walks the rows
     * and the inner loop walks each code inside the array for that row.
     *
     * A null entry inside the array is flagged separately from an invalid
     * code so the error messages are unambiguous.
     */
    private void checkCountryArrayColumns(ValidationReport report, DataFrame df) {
        String[] arrayCountryColumns = {
                "Countries of Outward Payments",
                "Countries of Inward Payments"
        };

        for (String colName : arrayCountryColumns) {
            Series<?> series = getSeries(df, colName);
            if (series == null) continue;

            for (int i = 0; i < series.size(); i++) {
                Object value = series.get(i);
                if (value == null) continue;
                if (!(value instanceof String[] countries)) continue; // type mismatch already caught by SchemaValidator

                for (String code : countries) {
                    if (code == null) {
                        report.addIssue(new ValidationIssue(
                                ValidationIssue.Severity.ERROR,
                                colName,
                                "A country code entry within the list is null.",
                                i
                        ));
                    } else if (!IsoCountryCodes.isValid(code)) {
                        report.addIssue(new ValidationIssue(
                                ValidationIssue.Severity.ERROR,
                                colName,
                                String.format("'%s' in the country list is not a valid ISO 3166-1 alpha-2 code.", code),
                                i
                        ));
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Safely retrieves a Series from the DataFrame by column name.
     * Returns null — rather than throwing — if the column does not exist,
     * since a missing column will already have been reported as an ERROR
     * by SchemaValidator and there is nothing more to check here.
     */
    private Series<?> getSeries(DataFrame df, String columnName) {
        try {
            return df.getColumn(columnName);
        } catch (Exception e) {
            return null;
        }
    }
}
