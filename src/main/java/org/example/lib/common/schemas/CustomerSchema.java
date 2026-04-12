package org.example.lib.common.schemas;

import org.example.lib.common.definitions.ColumnDefinition;
import org.example.lib.validator.ValidationReport;

import java.util.List;

public abstract class CustomerSchema {

    public abstract List<ColumnDefinition> getColumnDefinitions();

    public abstract String getSchemaName();

    public void performCrossColumnChecks(ValidationReport report, org.dflib.DataFrame df) {
        // Default: no cross-column checks. Subclasses override as needed.
    }

    public ColumnDefinition getDefinitionFor(String columnName) {
        return getColumnDefinitions().stream()
                .filter(cd -> cd.getColumnName().equalsIgnoreCase(columnName))
                .findFirst()
                .orElse(null);
    }
}