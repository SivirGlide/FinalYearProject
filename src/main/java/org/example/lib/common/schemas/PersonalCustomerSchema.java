package org.example.lib.common.schemas;

import org.example.lib.common.definitions.ColumnDefinition;
import org.example.lib.validator.ValidationReport;
import org.example.lib.validator.ValidationIssue;

import java.util.List;
import java.util.Set;

import static org.example.lib.common.definitions.ColumnDefinition.ColumnType.*;

public class PersonalCustomerSchema extends DataFrameSchema {

    private static final Set<String> CASH_TURNOVER_VALUES = Set.of(
            "0-10000",
            "10001-50000",
            "50001-100000",
            "100001-500000",
            "500001+"
    );

    @Override
    public String getSchemaName() {
        return "PersonalCustomerSchema";
    }
    @Override
    public List<ColumnDefinition> getColumnDefinitions() {
        return List.of(

                new ColumnDefinition.Builder("firstname", STRING)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Customer's first name")
                        .build(),

                new ColumnDefinition.Builder("middlename", STRING)
                        .required(true)
                        .nullsAllowed(true)   // Middle name may legitimately be absent
                        .comment("Customer's middle name — may be null if not applicable")
                        .build(),

                new ColumnDefinition.Builder("lastname", STRING)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Customer's last name")
                        .build(),

                new ColumnDefinition.Builder("Date Of Birth", DATE)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Must be a valid date; outlier check flags implausible DOBs")
                        .build(),

                new ColumnDefinition.Builder("Address", STRING)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Customer's full address")
                        .build(),

                new ColumnDefinition.Builder("Nationality", STRING)
                        .required(true)
                        .nullsAllowed(false)
                        .exactLength(2)
                        .comment("2-character ISO 3166-1 alpha-2 country code, e.g. GB, US, DE")
                        .build(),

                new ColumnDefinition.Builder("Country Of Birth", STRING)
                        .required(true)
                        .nullsAllowed(false)
                        .exactLength(2)
                        .comment("2-character ISO 3166-1 alpha-2 country code")
                        .build(),

                new ColumnDefinition.Builder("Residence", STRING)
                        .required(true)
                        .nullsAllowed(false)
                        .exactLength(2)
                        .comment("2-character ISO 3166-1 alpha-2 country code")
                        .build(),

                new ColumnDefinition.Builder("Cash Turnover", STRING)
                        .required(true)
                        .nullsAllowed(false)
                        .allowedValues(CASH_TURNOVER_VALUES)
                        .comment("Fixed dropdown value representing annual cash turnover band")
                        .build(),

                new ColumnDefinition.Builder("Countries of Outward Payments", STRING_ARRAY)
                        .required(true)
                        .nullsAllowed(false)  //minimum of one country
                        .comment("List of 2-char ISO codes for countries receiving payments from this customer")
                        .build(),

                new ColumnDefinition.Builder("Countries of Inward Payments", STRING_ARRAY)
                        .required(true)
                        .nullsAllowed(false)   //minimum of one country
                        .comment("List of 2-char ISO codes for countries sending payments to this customer")
                        .build()
        );
    }

    @Override
    public void performCrossColumnChecks(ValidationReport report, org.dflib.DataFrame df) {

        if (!df.getColumnsIndex().contains("Date Of Birth")) {
            return; // Column-level check will already have flagged this as missing
        }

        java.time.LocalDate today = java.time.LocalDate.now();

        org.dflib.Series<?> dobSeries = df.getColumn("Date Of Birth");

        for (int i = 0; i < dobSeries.size(); i++) {
            Object raw = dobSeries.get(i);
            if (raw == null) continue; // Null already flagged in column check

            if (raw instanceof java.time.LocalDate dob) {
                int age = java.time.Period.between(dob, today).getYears();

                if (age < 18) {
                    report.addIssue(new ValidationIssue(
                            ValidationIssue.Severity.WARNING,
                            "Date Of Birth",
                            String.format("Customer appears to be under 18 (age ~%d). Verify DOB: %s", age, dob),
                            i
                    ));
                } else if (age > 120) {
                    report.addIssue(new ValidationIssue(
                            ValidationIssue.Severity.WARNING,
                            "Date Of Birth",
                            String.format("Implausible age ~%d years — possible data entry error. DOB: %s", age, dob),
                            i
                    ));
                }
            }
        }
    }
}