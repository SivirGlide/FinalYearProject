package org.example.lib.common.schemas;

import org.example.lib.common.definitions.ColumnDefinition;
import org.example.lib.validator.ValidationIssue;
import org.example.lib.validator.ValidationReport;

import java.util.List;
import java.util.Set;

import static org.example.lib.common.definitions.ColumnDefinition.ColumnType.*;

public class BusinessCustomerSchema extends DataFrameSchema {

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