package org.example.lib.validator;

import org.dflib.DataFrame;
import org.dflib.Series;
import org.example.lib.common.definitions.ColumnDefinition;
import org.example.lib.common.schemas.DataFrameSchema;

import java.time.LocalDate;
import java.util.List;


public class SchemaValidator {

    private final DataFrameSchema schema;

    public SchemaValidator(DataFrameSchema schema) {
        this.schema = schema;
    }

    public ValidationReport validate(DataFrame df) {

        long rowCount = df.height(); // DFLib: .height() = number of rows
        ValidationReport report = new ValidationReport(schema.getSchemaName(), rowCount);

        List<ColumnDefinition> definitions = schema.getColumnDefinitions();

        // ── Step 1: Check for missing required columns ───────────────────────
        for (ColumnDefinition colDef : definitions) {
            if (colDef.isRequired() && !df.getColumnsIndex().contains(colDef.getColumnName())) {
                report.addIssue(new ValidationIssue(
                        ValidationIssue.Severity.ERROR,
                        colDef.getColumnName(),
                        "Required column is missing from the DataFrame."
                ));
            }
        }

        // ── Step 2: Per-column checks for columns that are present ───────────
        for (ColumnDefinition colDef : definitions) {
            String colName = colDef.getColumnName();

            if (!df.getColumnsIndex().contains(colName)) {
                continue; // Already flagged as missing above
            }

            Series<?> series = df.getColumn(colName);

            checkTypes(report, colDef, series);
            checkNulls(report, colDef, series);
            checkExactLength(report, colDef, series);
            checkAllowedValues(report, colDef, series);
            checkStringArrayElements(report, colDef, series);
        }

        // ── Step 3: Cross-column / business logic checks ─────────────────────
        // Delegates to the schema subclass (e.g. age outlier check in PersonalCustomerSchema)
        schema.performCrossColumnChecks(report, df);

        // ── Step 4: Flag unexpected columns (columns in DF not in schema) ────
        checkUnexpectedColumns(report, df, definitions);

        return report;
    }

    private void checkTypes(ValidationReport report, ColumnDefinition colDef, Series<?> series) {
        String colName = colDef.getColumnName();
        ColumnDefinition.ColumnType expectedType = colDef.getExpectedType();

        // Find first non-null value to inspect
        Object sample = null;
        for (int i = 0; i < series.size(); i++) {
            if (series.get(i) != null) {
                sample = series.get(i);
                break;
            }
        }

        if (sample == null) {
            // All values are null — can't check type, will be caught by null check
            return;
        }

        boolean typeOk = switch (expectedType) {
            case STRING       -> sample instanceof String;
            case DATE         -> sample instanceof LocalDate;
            case TIME -> false;
            case INT -> false;
            case STRING_ARRAY -> sample instanceof String[];
        };

        if (!typeOk) {
            report.addIssue(new ValidationIssue(
                    ValidationIssue.Severity.ERROR,
                    colName,
                    String.format(
                            "Type mismatch. Expected: %s, but found: %s (sample value class: %s).",
                            expectedType,
                            sample.getClass().getSimpleName(),
                            sample.getClass().getName()
                    )
            ));
        }
    }

    private void checkNulls(ValidationReport report, ColumnDefinition colDef, Series<?> series) {
        if (colDef.isNullsAllowed()) return; // Nothing to check

        String colName = colDef.getColumnName();
        int nullCount = 0;
        int firstNullRow = -1;

        for (int i = 0; i < series.size(); i++) {
            if (series.get(i) == null) {
                nullCount++;
                if (firstNullRow < 0) firstNullRow = i;
            }
        }

        if (nullCount > 0) {
            report.addIssue(new ValidationIssue(
                    ValidationIssue.Severity.WARNING,
                    colName,
                    String.format(
                            "Column does not allow nulls but contains %d null value(s). First occurrence at row %d.",
                            nullCount, firstNullRow
                    )
            ));
        }
    }

    private void checkExactLength(ValidationReport report, ColumnDefinition colDef, Series<?> series) {
        int requiredLength = colDef.getExactLength();
        if (requiredLength < 0) return; // No length constraint on this column

        String colName = colDef.getColumnName();

        for (int i = 0; i < series.size(); i++) {
            Object val = series.get(i);
            if (val == null) continue; // Null handled separately

            if (val instanceof String str && str.length() != requiredLength) {
                report.addIssue(new ValidationIssue(
                        ValidationIssue.Severity.WARNING,
                        colName,
                        String.format(
                                "Expected exactly %d characters but found \"%s\" (%d chars).",
                                requiredLength, str, str.length()
                        ),
                        i
                ));
            }
        }
    }

    private void checkAllowedValues(ValidationReport report, ColumnDefinition colDef, Series<?> series) {
        if (colDef.getAllowedValues().isEmpty()) return; // No constraint

        String colName = colDef.getColumnName();

        for (int i = 0; i < series.size(); i++) {
            Object val = series.get(i);
            if (val == null) continue;

            String strVal = val.toString();
            if (!colDef.getAllowedValues().contains(strVal)) {
                report.addIssue(new ValidationIssue(
                        ValidationIssue.Severity.WARNING,
                        colName,
                        String.format(
                                "Value \"%s\" is not in the allowed set: %s.",
                                strVal, colDef.getAllowedValues()
                        ),
                        i
                ));
            }
        }
    }

    private void checkStringArrayElements(ValidationReport report, ColumnDefinition colDef, Series<?> series) {
        if (colDef.getExpectedType() != ColumnDefinition.ColumnType.STRING_ARRAY) return;

        String colName = colDef.getColumnName();

        for (int i = 0; i < series.size(); i++) {
            Object val = series.get(i);
            if (val == null) continue;

            if (val instanceof String[] codes) {
                for (String code : codes) {
                    if (code == null) {
                        report.addIssue(new ValidationIssue(
                                ValidationIssue.Severity.WARNING,
                                colName,
                                "Array element is null inside the list.",
                                i
                        ));
                    } else if (code.length() != 2) {
                        report.addIssue(new ValidationIssue(
                                ValidationIssue.Severity.WARNING,
                                colName,
                                String.format(
                                        "Array element \"%s\" is not a valid 2-char ISO code (length: %d).",
                                        code, code.length()
                                ),
                                i
                        ));
                    }
                }
            } else {
                // Cell value is not a String[] — type mismatch already reported, skip
            }
        }
    }

    private void checkUnexpectedColumns(ValidationReport report, DataFrame df, List<ColumnDefinition> definitions) {
        List<String> definedNames = definitions.stream()
                .map(ColumnDefinition::getColumnName)
                .map(String::toLowerCase)
                .toList();

        for (String actualCol : df.getColumnsIndex()) {
            if (!definedNames.contains(actualCol.toLowerCase())) {
                report.addIssue(new ValidationIssue(
                        ValidationIssue.Severity.INFO,
                        actualCol,
                        "Column exists in DataFrame but is not defined in the schema. " +
                                "Verify this column is expected or update the schema."
                ));
            }
        }
    }
}