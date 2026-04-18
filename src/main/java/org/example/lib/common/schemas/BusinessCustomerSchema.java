package org.example.lib.common.schemas;

import org.example.lib.common.definitions.ColumnDefinition;
import org.example.lib.common.definitions.StandardColumnType;

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

                new ColumnDefinition.Builder("firstname", StandardColumnType.STRING)
                        .required(true)
                        .nullsAllowed(false)
                        .comment("Customer's first name")
                        .build()
        );
    }
}