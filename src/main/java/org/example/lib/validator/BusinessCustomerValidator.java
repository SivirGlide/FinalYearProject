package org.example.lib.validator;

import org.dflib.DataFrame;
import org.dflib.Series;
import org.example.lib.common.countries.IsoCountryCodes;
import org.example.lib.common.schemas.BusinessCustomerSchema;
import org.example.lib.validator.report.ValidationIssue;
import org.example.lib.validator.report.ValidationReport;

import java.time.LocalDate;

public class BusinessCustomerValidator implements DataValidator {

    public BusinessCustomerValidator() {}

    private static final BusinessCustomerSchema SCHEMA = new BusinessCustomerSchema();

    @Override
    public ValidationReport validate(DataFrame df) {
        ValidationReport report = new SchemaValidator(SCHEMA).validate(df);
        return this.CustomValidation(report, df);
    }

    protected ValidationReport CustomValidation(ValidationReport report, DataFrame df) {
        checkDateOfIncorporation(df, report);
        checkPeopleList(df, report);
        checkCashTurnoverVsTurnover(df, report);
        checkCountryCodes(df, report);
        return report;
    }

    // -------------------------------------------------------------------------
    // Custom validation checks
    // -------------------------------------------------------------------------

    private void checkDateOfIncorporation(DataFrame df, ValidationReport report) {
        Series<?> series;
        try {
            series = df.getColumn("dateofincorporation");
        } catch (Exception e) {
            return;
        }

        LocalDate today = LocalDate.now();
        for (int i = 0; i < series.size(); i++) {
            Object val = series.get(i);
            if (val == null) continue;
            if (!(val instanceof LocalDate date)) continue;
            if (date.isAfter(today)) {
                report.addIssue(new ValidationIssue(
                        ValidationIssue.Severity.ERROR,
                        "dateofincorporation",
                        String.format("Date of incorporation '%s' is in the future.", date),
                        i
                ));
            }
        }
    }

    private void checkPeopleList(DataFrame df, ValidationReport report) {
        Series<?> series;
        try {
            series = df.getColumn("peoplelist");
        } catch (Exception e) {
            return;
        }

        for (int i = 0; i < series.size(); i++) {
            Object val = series.get(i);
            boolean empty = (val == null)
                    || (val instanceof Object[] arr && arr.length == 0)
                    || (val instanceof String str && str.isBlank());

            if (empty) {
                report.addIssue(new ValidationIssue(
                        ValidationIssue.Severity.ERROR,
                        "peoplelist",
                        "People list must not be empty.",
                        i
                ));
            }
        }
    }

    private void checkCashTurnoverVsTurnover(DataFrame df, ValidationReport report) {
        Series<?> cashSeries;
        Series<?> turnoverSeries;
        try {
            cashSeries     = df.getColumn("cashturnover");
            turnoverSeries = df.getColumn("turnover");
        } catch (Exception e) {
            return;
        }

        for (int i = 0; i < cashSeries.size(); i++) {
            Object cashVal     = cashSeries.get(i);
            Object turnoverVal = turnoverSeries.get(i);
            if (cashVal == null || turnoverVal == null) continue;

            String band = cashVal.toString();
            if (band.endsWith("+")) continue;

            String[] parts = band.split("-");
            if (parts.length != 2) continue;

            long cashUpperBound;
            double turnover;
            try {
                cashUpperBound = Long.parseLong(parts[1].trim());
                turnover       = ((Number) turnoverVal).doubleValue();
            } catch (NumberFormatException | ClassCastException ex) {
                continue;
            }

            if (cashUpperBound > turnover) {
                report.addIssue(new ValidationIssue(
                        ValidationIssue.Severity.ERROR,
                        "cashturnover",
                        String.format(
                                "Cash turnover band '%s' (upper bound %d) exceeds reported turnover of %.2f.",
                                band, cashUpperBound, turnover),
                        i
                ));
            }
        }
    }

    private void checkCountryCodes(DataFrame df, ValidationReport report) {
        checkSingleCountryColumn(df, report, "countryofincorporation");
        checkArrayCountryColumn(df, report, "countriesofinwardpayments");
        checkArrayCountryColumn(df, report, "countriesofoutwardpayments");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void checkSingleCountryColumn(DataFrame df, ValidationReport report, String columnName) {
        Series<?> series;
        try {
            series = df.getColumn(columnName);
        } catch (Exception e) {
            return;
        }

        for (int i = 0; i < series.size(); i++) {
            Object val = series.get(i);
            if (val == null) continue;
            String code = val.toString();
            if (!IsoCountryCodes.isValid(code)) {
                report.addIssue(new ValidationIssue(
                        ValidationIssue.Severity.ERROR,
                        columnName,
                        String.format("'%s' is not a valid ISO 3166-1 alpha-2 country code.", code),
                        i
                ));
            }
        }
    }

    private void checkArrayCountryColumn(DataFrame df, ValidationReport report, String columnName) {
        Series<?> series;
        try {
            series = df.getColumn(columnName);
        } catch (Exception e) {
            return;
        }

        for (int i = 0; i < series.size(); i++) {
            Object val = series.get(i);
            if (val == null) continue;
            if (!(val instanceof Object[] codes)) continue;

            for (Object entry : codes) {
                if (entry == null) continue;
                String code = entry.toString();
                if (!IsoCountryCodes.isValid(code)) {
                    report.addIssue(new ValidationIssue(
                            ValidationIssue.Severity.ERROR,
                            columnName,
                            String.format("'%s' is not a valid ISO 3166-1 alpha-2 country code.", code),
                            i
                    ));
                }
            }
        }
    }
}
