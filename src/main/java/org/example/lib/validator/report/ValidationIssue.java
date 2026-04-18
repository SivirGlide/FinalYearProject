package org.example.lib.validator.report;
/**
 * Represents a single problem found during validation.
 *
 * Issues have a Severity so the caller can decide how to handle them:
 *   ERROR   → the data is definitively wrong (missing column, wrong type)
 *   WARNING → something suspicious that might be wrong (nulls, outliers, format issues)
 *   INFO    → informational observations that aren't necessarily problems
 */
public class ValidationIssue {

    public enum Severity {
        ERROR,    // Must fix — data cannot be trusted as-is
        WARNING,  // Should investigate — might be intentional or might be a data quality problem
        INFO      // Just so you know
    }

    private final Severity severity;
    private final String columnName;   // Which column triggered this issue ("N/A" if schema-level)
    private final String description;  // Human-readable explanation
    private final long rowIndex;       // -1 if not row-specific (e.g. whole-column issues)

    public ValidationIssue(Severity severity, String columnName, String description, long rowIndex) {
        this.severity    = severity;
        this.columnName  = columnName;
        this.description = description;
        this.rowIndex    = rowIndex;
    }

    public ValidationIssue(Severity severity, String columnName, String description) {
        this(severity, columnName, description, -1L);
    }

    public Severity getSeverity()    { return severity;    }
    public String   getColumnName()  { return columnName;  }
    public String   getDescription() { return description; }
    public long     getRowIndex()    { return rowIndex;    }

    @Override
    public String toString() {
        String location = (rowIndex >= 0)
                ? String.format("[Column: %-30s | Row: %d]", columnName, rowIndex)
                : String.format("[Column: %-30s]", columnName);

        return String.format("%-8s %s %s", severity, location, description);
    }
}
