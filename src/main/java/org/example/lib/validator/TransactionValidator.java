package org.example.lib.validator;

import org.dflib.DataFrame;
import org.dflib.Series;
import org.example.lib.common.countries.IsoCountryCodes;
import org.example.lib.common.schemas.TransactionSchema;
import org.example.lib.validator.report.ValidationIssue;
import org.example.lib.validator.report.ValidationReport;

import java.time.LocalDate;
import java.time.LocalTime;

public class TransactionValidator implements DataValidator {

    public TransactionValidator() {}

    private static final TransactionSchema SCHEMA = new TransactionSchema();

    @Override
    public ValidationReport validate(DataFrame df) {
        ValidationReport report = new SchemaValidator(SCHEMA).validate(df);
        return this.CustomValidation(report, df);
    }

    protected ValidationReport CustomValidation(ValidationReport report, DataFrame df) {
        checkDateAndTime(df, report);
        checkAccountNumber(df, report);
        checkSortCode(df, report);
        checkCountryCodes(df, report);
        return report;
    }

    // -------------------------------------------------------------------------
    // Custom validation checks
    // -------------------------------------------------------------------------

    private void checkDateAndTime(DataFrame df, ValidationReport report) {
        Series<?> dateSeries;
        Series<?> timeSeries;
        try {
            dateSeries = df.getColumn("date");
            timeSeries = df.getColumn("time");
        } catch (Exception e) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalTime now   = LocalTime.now();

        for (int i = 0; i < dateSeries.size(); i++) {
            Object dateVal = dateSeries.get(i);
            Object timeVal = timeSeries.get(i);
            if (dateVal == null || !(dateVal instanceof LocalDate date)) continue;

            if (date.isAfter(today)) {
                report.addIssue(new ValidationIssue(
                        ValidationIssue.Severity.ERROR,
                        "date",
                        String.format("Transaction date '%s' is in the future.", date),
                        i
                ));
            } else if (date.isEqual(today) && timeVal instanceof LocalTime time && time.isAfter(now)) {
                report.addIssue(new ValidationIssue(
                        ValidationIssue.Severity.ERROR,
                        "time",
                        String.format("Transaction time '%s' is in the future.", time),
                        i
                ));
            }
        }
    }

    private void checkAccountNumber(DataFrame df, ValidationReport report) {
        Series<?> series;
        try {
            series = df.getColumn("accountnumber");
        } catch (Exception e) {
            return;
        }

        for (int i = 0; i < series.size(); i++) {
            Object val = series.get(i);
            if (val == null) continue;
            String digits = val.toString().replaceAll("\\s", "");
            if (digits.length() != 8 || !digits.chars().allMatch(Character::isDigit)) {
                report.addIssue(new ValidationIssue(
                        ValidationIssue.Severity.ERROR,
                        "accountnumber",
                        String.format("Account number '%s' must be exactly 8 digits.", val),
                        i
                ));
            }
        }
    }

    private void checkSortCode(DataFrame df, ValidationReport report) {
        Series<?> series;
        try {
            series = df.getColumn("beneficiarysortcode");
        } catch (Exception e) {
            return;
        }

        for (int i = 0; i < series.size(); i++) {
            Object val = series.get(i);
            if (val == null) continue;
            String digits = val.toString().replaceAll("[^0-9]", "");
            if (digits.length() != 6) {
                report.addIssue(new ValidationIssue(
                        ValidationIssue.Severity.ERROR,
                        "beneficiarysortcode",
                        String.format("Sort code '%s' must be exactly 6 digits.", val),
                        i
                ));
            }
        }
    }

    private void checkCountryCodes(DataFrame df, ValidationReport report) {
        for (String column : new String[]{"countryoforigin", "countryofbeneficiary"}) {
            Series<?> series;
            try {
                series = df.getColumn(column);
            } catch (Exception e) {
                continue;
            }

            for (int i = 0; i < series.size(); i++) {
                Object val = series.get(i);
                if (val == null) continue;
                String code = val.toString();
                if (!IsoCountryCodes.isValid(code)) {
                    report.addIssue(new ValidationIssue(
                            ValidationIssue.Severity.ERROR,
                            column,
                            String.format("'%s' is not a valid ISO 3166-1 alpha-2 country code.", code),
                            i
                    ));
                }
            }
        }
    }
}
