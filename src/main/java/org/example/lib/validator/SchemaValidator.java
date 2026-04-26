package org.example.lib.validator;

import org.dflib.DataFrame;
import org.dflib.Series;
import org.example.lib.common.columns.ColumnDefinition;
import org.example.lib.common.columns.StandardColumnType;
import org.example.lib.common.schemas.DataFrameSchema;
import org.example.lib.validator.report.ValidationIssue;
import org.example.lib.validator.report.ValidationReport;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SchemaValidator {

    private final DataFrameSchema schema;

    public SchemaValidator(DataFrameSchema schema) {
        this.schema = schema;
    }

    /**
     * Runs all four validation checks against the provided DataFrame and
     * returns a report containing every issue found.
     */
    public ValidationReport validate(DataFrame df) {
        ValidationReport report = new ValidationReport(schema.getSchemaName(), df.height());

        // Build a case-insensitive map of  "lowercaseName -> actualName"  so that
        // every private method can resolve the real column label used in the DataFrame.
        Map<String, String> dfColumnMap = buildColumnMap(df);

        checkColumns(dfColumnMap, report);
        checkColumnTypes(df, dfColumnMap, report);
        checkNullValues(df, dfColumnMap, report);
        checkAllowedValues(df, dfColumnMap, report);

        return report;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Builds a map of lowercase column name → actual column label as it appears
     * in the DataFrame.  This lets every check do case-insensitive look-ups
     * (consistent with DataFrameSchema#getDefinitionFor).
     */
    private Map<String, String> buildColumnMap(DataFrame df) {
        Map<String, String> map = new HashMap<>();
        for (String label : df.getColumnsIndex()) {
            map.put(label.toLowerCase(), label);
        }
        return map;
    }

    /**
     * TASK 1 – Column presence check.
     *
     * Every column that is marked required="true" in the schema MUST exist in
     * the DataFrame.  If it is absent an ERROR is recorded.
     *
     * Non-required columns are simply optional; their absence is fine.
     */
    private void checkColumns(Map<String, String> dfColumnMap, ValidationReport report) {
        for (ColumnDefinition colDef : schema.getColumnDefinitions()) {
            String key = colDef.getColumnName().toLowerCase();
            if (!dfColumnMap.containsKey(key)) {
                report.addIssue(new ValidationIssue(
                        ValidationIssue.Severity.ERROR,
                        colDef.getColumnName(),
                        "Column defined in schema is missing from the DataFrame."
                ));
            }
        }
    }

    /**
     * TASK 2 – Column type check.
     *
     * For every schema column that is present in the DataFrame, the nominal
     * Java type of the DFLib Series is compared against the expected type from
     * the schema.  A mismatch is recorded as an ERROR.
     *
     * Only StandardColumnType values are handled here; custom ColumnType
     * implementations are skipped because we have no mapping for them.
     */
    private void checkColumnTypes(DataFrame df,
                                  Map<String, String> dfColumnMap,
                                  ValidationReport report) {

        for (ColumnDefinition colDef : schema.getColumnDefinitions()) {
            String actualLabel = resolveLabel(colDef.getColumnName(), dfColumnMap);
            if (actualLabel == null) continue; // missing column already flagged in checkColumns

            // Only handle the known StandardColumnType enum values
            if (!(colDef.getExpectedType() instanceof StandardColumnType)) continue;

            Class<?> expectedClass = toJavaType((StandardColumnType) colDef.getExpectedType());
            if (expectedClass == null) continue;

            // Check each non-null value's runtime class against the expected type.
            // Nulls are skipped here — they are handled separately in checkNullValues.
            Series<?> series = df.getColumn(actualLabel);
            for (int i = 0; i < series.size(); i++) {
                Object value = series.get(i);
                if (value == null) continue;

                // isAssignableFrom allows for subclass relationships (e.g. Integer is a Number)
                if (!expectedClass.isAssignableFrom(value.getClass())) {
                    report.addIssue(new ValidationIssue(
                            ValidationIssue.Severity.ERROR,
                            colDef.getColumnName(),
                            String.format(
                                    "Type mismatch: schema expects %s (%s) but found value '%s' of type %s.",
                                    colDef.getExpectedType().name(),
                                    expectedClass.getSimpleName(),
                                    value,
                                    value.getClass().getSimpleName()
                            ),
                            i
                    ));
                }
            }
        }
    }

    /**
     * TASK 3 – Null value check.
     *
     * Iterates every value in each schema column and counts nulls.
     *
     *  • If the column disallows nulls (nullsAllowed = false) AND nulls are
     *    found → ERROR  (the data is definitively wrong).
     *  • If the column allows nulls but some are present → WARNING
     *    (informing the caller how many nulls exist so they can decide whether
     *    that is expected or suspicious).
     */
    private void checkNullValues(DataFrame df,
                                 Map<String, String> dfColumnMap,
                                 ValidationReport report) {

        for (ColumnDefinition colDef : schema.getColumnDefinitions()) {
            String actualLabel = resolveLabel(colDef.getColumnName(), dfColumnMap);
            if (actualLabel == null) continue;

            Series<?> series = df.getColumn(actualLabel);
            long nullCount = 0;

            for (int i = 0; i < series.size(); i++) {
                if (series.get(i) == null) {
                    nullCount++;
                }
            }

            if (nullCount == 0) continue; // nothing to report

            if (!colDef.isNullsAllowed()) {
                report.addIssue(new ValidationIssue(
                        ValidationIssue.Severity.ERROR,
                        colDef.getColumnName(),
                        String.format(
                                "Column does not allow null values but contains %d null(s) out of %d row(s).",
                                nullCount, series.size()
                        )
                ));
            } else {
                report.addIssue(new ValidationIssue(
                        ValidationIssue.Severity.WARNING,
                        colDef.getColumnName(),
                        String.format(
                                "Column contains %d null value(s) out of %d row(s).",
                                nullCount, series.size()
                        )
                ));
            }
        }
    }

    /**
     * TASK 4 – Allowed-values check.
     *
     * Some columns in the schema declare a finite set of permitted values
     * (e.g. a STATUS column that may only hold "ACTIVE", "INACTIVE", "PENDING").
     * If a non-null cell value is not in that set an ERROR is recorded, including
     * the exact row index so the caller can locate the offending record.
     *
     * Null values are intentionally skipped here — they are already handled by
     * the null check above, keeping the two concerns separate.
     */
    private void checkAllowedValues(DataFrame df,
                                    Map<String, String> dfColumnMap,
                                    ValidationReport report) {

        for (ColumnDefinition colDef : schema.getColumnDefinitions()) {
            Set<String> allowed = colDef.getAllowedValues();
            if (allowed.isEmpty()) continue; // no constraint defined for this column

            String actualLabel = resolveLabel(colDef.getColumnName(), dfColumnMap);
            if (actualLabel == null) continue;

            Series<?> series = df.getColumn(actualLabel);

            for (int i = 0; i < series.size(); i++) {
                Object value = series.get(i);
                if (value == null) continue; // null handled separately

                String strValue = value.toString();
                if (!allowed.contains(strValue)) {
                    report.addIssue(new ValidationIssue(
                            ValidationIssue.Severity.ERROR,
                            colDef.getColumnName(),
                            String.format(
                                    "Value '%s' is not in the allowed set %s.",
                                    strValue, allowed
                            ),
                            i   // row index stored so the caller knows exactly which row is bad
                    ));
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Returns the real DataFrame column label that matches the schema column
     * name case-insensitively, or null if no such column exists in the DataFrame.
     */
    private String resolveLabel(String schemaColumnName, Map<String, String> dfColumnMap) {
        return dfColumnMap.get(schemaColumnName.toLowerCase());
    }

    /**
     * Maps a StandardColumnType to its corresponding Java class.
     * This is used by checkColumnTypes to compare against DFLib's Series.getType().
     */
    private Class<?> toJavaType(StandardColumnType type) {
        return switch (type) {
            case STRING                  -> String.class;
            case DATE                    -> LocalDate.class;
            case TIME                    -> LocalTime.class;
            case INT                     -> Integer.class;
            case STRING_ARRAY            -> String[].class;
            case PERSONAL_CUSTOMER_ARRAY -> Object[].class;
        };
    }
}