package org.example.lib.common.schemas;

import org.example.lib.common.definitions.ColumnDefinition;
import org.example.lib.common.definitions.StandardColumnType;

import java.util.List;

public class TransactionSchema extends DataFrameSchema {

    @Override
    public String getSchemaName() {
        return "TransactionSchema";
    }

    @Override
    public List<ColumnDefinition> getColumnDefinitions() {
        return List.of(

                new ColumnDefinition.Builder("customernumber", StandardColumnType.INT)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Unique identifier linking this transaction to a customer record")
                        .build(),

                new ColumnDefinition.Builder("accountnumber", StandardColumnType.INT)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Account number the transaction was made from")
                        .build(),

                new ColumnDefinition.Builder("date", StandardColumnType.DATE)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Date the transaction occurred")
                        .build(),

                new ColumnDefinition.Builder("time", StandardColumnType.TIME)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Time the transaction occurred")
                        .build(),

                new ColumnDefinition.Builder("beneficiaryname", StandardColumnType.STRING)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Full name of the payment beneficiary")
                        .build(),

                new ColumnDefinition.Builder("beneficiaryaccountnumber", StandardColumnType.INT)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Account number belonging to the beneficiary")
                        .build(),

                new ColumnDefinition.Builder("beneficiarysortcode", StandardColumnType.INT)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Sort code of the beneficiary's bank")
                        .build(),

                new ColumnDefinition.Builder("beneficiaryreference", StandardColumnType.STRING)
                        .required(true)
                        .nullsAllowed(true)  // Reference may legitimately be blank
                        .comment("Payment reference supplied by the sender")
                        .build(),

                new ColumnDefinition.Builder("purposeofpayment", StandardColumnType.STRING)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Stated purpose of the payment")
                        .build(),

                new ColumnDefinition.Builder("countryoforigin", StandardColumnType.STRING)
                        .required(true)
                        .nullsAllowed(false)
                        .exactLength(2)
                        .comment("2-character ISO 3166-1 alpha-2 code for the country this payment originates from")
                        .build(),

                new ColumnDefinition.Builder("countryofbeneficiary", StandardColumnType.STRING)
                        .required(true)
                        .nullsAllowed(false)
                        .exactLength(2)
                        .comment("2-character ISO 3166-1 alpha-2 code for the beneficiary's country")
                        .build()
        );
    }
}
