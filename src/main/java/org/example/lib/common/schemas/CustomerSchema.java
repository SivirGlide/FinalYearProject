package org.example.lib.common.schemas;

import org.example.lib.common.definitions.ColumnDefinition;
import org.example.lib.validator.ValidationReport;

import java.util.List;

public abstract class CustomerSchema {

    /**
     * Every subclass MUST implement this and return the full list of
     * ColumnDefinitions that describe that customer type's expected schema.
     *
     * Example implementation in PersonalCustomerSchema:
     *   return List.of(
     *       new ColumnDefinition.Builder("firstname", STRING).build(),
     *       ...
     *   );
     */
    public abstract List<ColumnDefinition> getColumnDefinitions();

    /**
     * A human-readable name used in reports, e.g. "PersonalCustomerSchema".
     * Subclasses should override this to return their own name.
     */
    public abstract String getSchemaName();

    /**
     * Optional hook — subclasses can override this to add schema-level checks
     * beyond per-column validation (e.g. cross-column rules like
     * "if field A is set, field B must also be set").
     *
     * The default implementation does nothing; override only when needed.
     *
     * @param report  The in-progress ValidationReport to add issues to.
     * @param df      The DFLib DataFrame being validated.
     */
    public void performCrossColumnChecks(ValidationReport report, org.dflib.DataFrame df) {
        // Default: no cross-column checks. Subclasses override as needed.
    }

    /**
     * Convenience method — looks up a ColumnDefinition by column name.
     * Returns null if no definition exists for that name.
     */
    public ColumnDefinition getDefinitionFor(String columnName) {
        return getColumnDefinitions().stream()
                .filter(cd -> cd.getColumnName().equalsIgnoreCase(columnName))
                .findFirst()
                .orElse(null);
    }
}