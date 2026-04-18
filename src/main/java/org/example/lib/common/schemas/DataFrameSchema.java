package org.example.lib.common.schemas;

import org.example.lib.common.columns.ColumnDefinition;

import java.util.List;

public abstract class DataFrameSchema {

    public abstract List<ColumnDefinition> getColumnDefinitions();

    public abstract String getSchemaName();


    public ColumnDefinition getDefinitionFor(String columnName) {
        return getColumnDefinitions().stream()
                .filter(cd -> cd.getColumnName().equalsIgnoreCase(columnName))
                .findFirst()
                .orElse(null);
    }
}