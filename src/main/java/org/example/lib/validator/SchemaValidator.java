package org.example.lib.validator;

import org.dflib.DataFrame;
import org.dflib.Series;
import org.example.lib.common.definitions.ColumnDefinition;
import org.example.lib.common.schemas.CustomerSchema;

import java.time.LocalDate;
import java.util.List;

/**
 * The main validation engine.
 *
 * HOW IT WORKS — step by step:
 *
 *  1. Check that every required column defined in the schema is present
 *     in the DataFrame. Missing → ERROR.
 *
 *  2. For each column that IS present, check:
 *       a. Type check       — do the actual values match the expected type?
 *       b. Null check       — any nulls in a column that shouldn't have them?
 *       c. Length check     — for 2-char ISO fields, is every value exactly 2 chars?
 *       d. Allowed values   — for dropdown fields, is every value in the allowed set?
 *       e. Array element    — for String[] columns, are the individual ISO codes valid?
 *
 *  3. Run the schema's cross-column checks (e.g. age outlier logic in PersonalCustomerSchema).
 *
 *  4. Flag unexpected columns — columns in the DataFrame that aren't in the schema at all
 *     (reported as INFO so you're aware without it being a hard failure).
 *
 * IMPORTANT: This class NEVER modifies the DataFrame. It is read-only.
 */
public class SchemaValidator {

    private final CustomerSchema schema;

    public SchemaValidator(CustomerSchema schema) {
        this.schema = schema;
    }

    /**
     * Run the full validation and return a report.
     *
     * @param df  The DFLib DataFrame to validate. Not modified.
     * @return    A ValidationReport containing all issues found.
     */
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

    // =========================================================================
    // Private check methods — each handles one category of validation
    // =========================================================================

    /**
     * TYPE CHECK
     *
     * DFLib Series are typed. We inspect the series' element type and
     * compare it against what the ColumnDefinition says it should be.
     *
     * DFLib uses generics — Series<String>, Series<LocalDate>, etc.
     * We look at a sample of non-null values to infer the actual type,
     * because DFLib may report Object for mixed/untyped columns.
     */
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

    /**
     * NULL CHECK
     *
     * Scans every row in the series. If nulls are NOT allowed and a null
     * is found, we emit a WARNING with the row index so you can find it.
     *
     * We emit one WARNING per null found so the report shows every affected row.
     * If a column has many nulls this can be verbose — you may want to add a
     * "max issues per column" cap for large DataFrames.
     */
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

    /**
     * EXACT LENGTH CHECK
     *
     * Used for 2-char ISO code fields (Nationality, Country Of Birth, Residence,
     * and elements of the payment country arrays).
     *
     * Flags values that aren't exactly the required length — e.g. "USA" (3 chars)
     * or "G" (1 char) would both be flagged for a field requiring length 2.
     */
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

    /**
     * ALLOWED VALUES CHECK
     *
     * For fields that must contain one of a fixed set of values (e.g. Cash Turnover
     * dropdown). Any value outside the set is flagged as a WARNING.
     */
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

    /**
     * STRING ARRAY ELEMENT CHECK
     *
     * For columns typed as STRING_ARRAY (Countries of Outward/Inward Payments),
     * each cell holds a String[]. This check inspects every element within
     * each array and flags elements that aren't exactly 2 characters long
     * (since they should be ISO country codes).
     */
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

    /**
     * UNEXPECTED COLUMN CHECK
     *
     * Finds columns in the DataFrame that aren't defined in the schema at all.
     * This is logged as INFO — it might be intentional (extra enrichment columns)
     * or it might mean a column was renamed and the schema needs updating.
     */
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