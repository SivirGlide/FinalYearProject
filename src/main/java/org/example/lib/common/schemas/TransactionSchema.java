package org.example.lib.common.schemas;

import org.example.lib.common.definitions.ColumnDefinition;
import org.example.lib.validator.ValidationIssue;
import org.example.lib.validator.ValidationReport;

import java.util.List;

import static org.example.lib.common.definitions.ColumnDefinition.ColumnType.*;

public class TransactionSchema extends DataFrameSchema {

    @Override
    public String getSchemaName() {
        return "TransactionSchema";
    }

    @Override
    public List<ColumnDefinition> getColumnDefinitions() {
        return List.of(

                // ── Customer and account identifiers ─────────────────────────────

                new ColumnDefinition.Builder("customernumber", INT)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Unique identifier linking this transaction to a customer record")
                        .build(),

                new ColumnDefinition.Builder("accountnumber", INT)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Account number the transaction was made from")
                        .build(),

                // ── Date and time ─────────────────────────────────────────────────

                new ColumnDefinition.Builder("date", DATE)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Date the transaction occurred")
                        .build(),

                new ColumnDefinition.Builder("time", TIME)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Time the transaction occurred")
                        .build(),

                // ── Beneficiary details ───────────────────────────────────────────

                new ColumnDefinition.Builder("beneficiaryname", STRING)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Full name of the payment beneficiary")
                        .build(),

                new ColumnDefinition.Builder("beneficiaryaccountnumber", INT)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Account number belonging to the beneficiary")
                        .build(),

                new ColumnDefinition.Builder("beneficiarysortcode", INT)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Sort code of the beneficiary's bank")
                        .build(),

                new ColumnDefinition.Builder("beneficiaryreference", STRING)
                        .required(true)
                        .nullsAllowed(true)  // Reference may legitimately be blank
                        .comment("Payment reference supplied by the sender")
                        .build(),

                // ── Payment metadata ──────────────────────────────────────────────

                new ColumnDefinition.Builder("purposeofpayment", STRING)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Stated purpose of the payment")
                        .build(),

                new ColumnDefinition.Builder("countryoforigin", STRING)
                        .required(true)
                        .nullsAllowed(false)
                        .exactLength(2)
                        .comment("2-character ISO 3166-1 alpha-2 code for the country this payment originates from")
                        .build(),

                new ColumnDefinition.Builder("countryofbeneficiary", STRING)
                        .required(true)
                        .nullsAllowed(false)
                        .exactLength(2)
                        .comment("2-character ISO 3166-1 alpha-2 code for the beneficiary's country")
                        .build()
        );
    }

    @Override
    public void performCrossColumnChecks(ValidationReport report, org.dflib.DataFrame df) {

        boolean hasOrigin      = df.getColumnsIndex().contains("countryoforigin");
        boolean hasBeneficiary = df.getColumnsIndex().contains("countryofbeneficiary");

        if (!hasOrigin || !hasBeneficiary) return; // Missing columns already flagged as errors

        org.dflib.Series<?> originSeries      = df.getColumn("countryoforigin");
        org.dflib.Series<?> beneficiarySeries = df.getColumn("countryofbeneficiary");

        for (int i = 0; i < originSeries.size(); i++) {
            Object origin      = originSeries.get(i);
            Object beneficiary = beneficiarySeries.get(i);

            if (origin == null || beneficiary == null) continue;

            if (origin.equals(beneficiary)) {
                report.addIssue(new ValidationIssue(
                        ValidationIssue.Severity.INFO,
                        "countryoforigin / countryofbeneficiary",
                        String.format(
                                "Domestic transaction — origin and beneficiary country are both \"%s\".", origin
                        ),
                        i
                ));
            }
        }
    }
}
