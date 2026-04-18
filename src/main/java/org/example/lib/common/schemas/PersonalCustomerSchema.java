package org.example.lib.common.schemas;

import org.example.lib.common.definitions.ColumnDefinition;
import org.example.lib.common.definitions.StandardColumnType;

import java.util.List;
import java.util.Set;

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

                new ColumnDefinition.Builder("firstname", StandardColumnType.STRING)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Customer's first name")
                        .build(),

                new ColumnDefinition.Builder("middlename", StandardColumnType.STRING)
                        .required(true)
                        .nullsAllowed(true)   // Middle name may legitimately be absent
                        .comment("Customer's middle name — may be null if not applicable")
                        .build(),

                new ColumnDefinition.Builder("lastname", StandardColumnType.STRING)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Customer's last name")
                        .build(),

                new ColumnDefinition.Builder("Date Of Birth", StandardColumnType.DATE)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Must be a valid date; outlier check flags implausible DOBs")
                        .build(),

                new ColumnDefinition.Builder("Address", StandardColumnType.STRING)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Customer's full address")
                        .build(),

                new ColumnDefinition.Builder("Nationality", StandardColumnType.STRING)
                        .required(true)
                        .nullsAllowed(false)
                        .exactLength(2)
                        .comment("2-character ISO 3166-1 alpha-2 country code, e.g. GB, US, DE")
                        .build(),

                new ColumnDefinition.Builder("Country Of Birth", StandardColumnType.STRING)
                        .required(true)
                        .nullsAllowed(false)
                        .exactLength(2)
                        .comment("2-character ISO 3166-1 alpha-2 country code")
                        .build(),

                new ColumnDefinition.Builder("Residence", StandardColumnType.STRING)
                        .required(true)
                        .nullsAllowed(false)
                        .exactLength(2)
                        .comment("2-character ISO 3166-1 alpha-2 country code")
                        .build(),

                new ColumnDefinition.Builder("Cash Turnover", StandardColumnType.STRING)
                        .required(true)
                        .nullsAllowed(false)
                        .allowedValues(CASH_TURNOVER_VALUES)
                        .comment("Fixed dropdown value representing annual cash turnover band")
                        .build(),

                new ColumnDefinition.Builder("Countries of Outward Payments", StandardColumnType.STRING_ARRAY)
                        .required(true)
                        .nullsAllowed(false)  //minimum of one country
                        .comment("List of 2-char ISO codes for countries receiving payments from this customer")
                        .build(),

                new ColumnDefinition.Builder("Countries of Inward Payments", StandardColumnType.STRING_ARRAY)
                        .required(true)
                        .nullsAllowed(false)   //minimum of one country
                        .comment("List of 2-char ISO codes for countries sending payments to this customer")
                        .build()
        );
    }
}