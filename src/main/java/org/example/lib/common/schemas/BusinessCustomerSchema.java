package org.example.lib.common.schemas;

import org.example.lib.common.columns.ColumnDefinition;
import org.example.lib.common.columns.StandardColumnType;

import java.util.List;

public class BusinessCustomerSchema extends DataFrameSchema {

    @Override
    public String getSchemaName() {
        return "BusinessCustomerSchema";
    }

    @Override
    public List<ColumnDefinition> getColumnDefinitions() {
        return List.of(

                new ColumnDefinition.Builder("dateofincorporation", StandardColumnType.DATE)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Date the business was legally incorporated")
                        .build(),

                new ColumnDefinition.Builder("people", StandardColumnType.PERSONAL_CUSTOMER_ARRAY)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Customers should be existing personal customers")
                        .build(),

                new ColumnDefinition.Builder("turnover", StandardColumnType.INT)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Annual turnover of the business")
                        .build(),

                new ColumnDefinition.Builder("siccode", StandardColumnType.STRING)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Gov.uk SIC code representing the business activity")
                        .build(),

                new ColumnDefinition.Builder("cashturnover", StandardColumnType.INT)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Annual cash turnover — must not exceed total turnover")
                        .build(),

                new ColumnDefinition.Builder("countryofincorporation", StandardColumnType.STRING)
                        .required(true)
                        .nullsAllowed(false)
                        .exactLength(2)
                        .comment("2-character ISO 3166-1 alpha-2 code for the country of incorporation")
                        .build(),

                new ColumnDefinition.Builder("countriesofoutwardpayments", StandardColumnType.STRING_ARRAY)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("List of 2-character ISO codes for countries receiving payments from this business")
                        .build(),

                new ColumnDefinition.Builder("countriesofinwardpayments", StandardColumnType.STRING_ARRAY)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("List of 2-character ISO codes for countries sending payments to this business")
                        .build()
        );
    }
}
