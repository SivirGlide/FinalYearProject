package org.example.domain.customer.reader;

import java.util.*;
import java.util.stream.Collectors;

public class DataSet {

    private final String source;
    private List<String> columns;
    private List<Map<String, String>> rows;

    public DataSet(String source, List<String> columns, List<Map<String, String>> rows) {
        this.source = source;
        this.columns = new ArrayList<>(columns);
        this.rows = new ArrayList<>(rows);
    }

    // =====================================================================
    // CORE ACCESS
    // =====================================================================

    public int rowCount() { return rows.size(); }
    public List<String> getColumns() { return columns; }
    public List<Map<String, String>> getAllRows() { return rows; }
    public String getSource() { return source; }

    public Map<String, String> getRow(int index) {
        if (index < 0 || index >= rows.size())
            throw new IndexOutOfBoundsException("Row index " + index + " out of bounds. Total rows: " + rows.size());
        return rows.get(index);
    }

    public Optional<String> getValue(int rowIndex, String column) {
        return Optional.ofNullable(getRow(rowIndex).get(column));
    }

    public List<String> getColumn(String columnName) {
        if (!columns.contains(columnName))
            throw new IllegalArgumentException("Column '" + columnName + "' does not exist.");
        return rows.stream()
                .map(row -> row.get(columnName))
                .collect(Collectors.toList());
    }

    public boolean hasColumn(String columnName) { return columns.contains(columnName); }

    // =====================================================================
    // DROP — remove rows or columns
    // =====================================================================

    /**
     * Drops a row by its index position.
     * e.g. dataSet.dropRow(0) — removes the first row
     */
    public DataSet dropRow(int index) {
        if (index < 0 || index >= rows.size())
            throw new IndexOutOfBoundsException("Row index " + index + " out of bounds.");
        rows.remove(index);
        return this; // return this so you can chain calls
    }

    /**
     * Drops every row where the given column matches the given value.
     * e.g. dataSet.dropRowsWhere("nationality", "N/A")
     */
    public DataSet dropRowsWhere(String column, String value) {
        assertColumnExists(column);
        rows.removeIf(row -> Objects.equals(row.get(column), value));
        return this;
    }

    /**
     * Drops an entire column from every row.
     * e.g. dataSet.dropColumn("middleName")
     */
    public DataSet dropColumn(String columnName) {
        assertColumnExists(columnName);
        columns.remove(columnName);
        rows.forEach(row -> row.remove(columnName));
        return this;
    }

    // =====================================================================
    // TYPE CONVERSION — convert a column's values to a different type
    // =====================================================================

    public enum ColumnType { INTEGER, DOUBLE, BOOLEAN, STRING }

    /**
     * Converts all values in a column to the specified type.
     * Values that cannot be converted are set to null.
     * e.g. dataSet.convertColumn("expectedCashTurnover", ColumnType.INTEGER)
     */
    public DataSet convertColumn(String columnName, ColumnType type) {
        assertColumnExists(columnName);

        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> row = rows.get(i);
            String raw = row.get(columnName);

            try {
                String converted = switch (type) {
                    case INTEGER -> String.valueOf(Integer.parseInt(raw.trim()));
                    case DOUBLE  -> String.valueOf(Double.parseDouble(raw.trim()));
                    case BOOLEAN -> String.valueOf(Boolean.parseBoolean(raw.trim()));
                    case STRING  -> raw; // already a string, nothing to do
                };
                row.put(columnName, converted);
            } catch (NumberFormatException | NullPointerException e) {
                System.out.println("Warning: Row " + (i + 1) + " could not convert '" + raw + "' to " + type + " in column '" + columnName + "' — set to null.");
                row.put(columnName, null);
            }
        }
        return this;
    }

    // =====================================================================
    // DESCRIBE — pandas-style summary statistics for numeric columns
    // =====================================================================

    /**
     * Prints a summary of statistics for all numeric columns — like pandas df.describe().
     * For each numeric column: count, mean, std dev, min, 25%, median, 75%, max.
     */
    public void describe() {
        System.out.println("\n=== DataSet: " + source + " | Rows: " + rowCount() + " | Columns: " + columns.size() + " ===\n");

        // Find every column that has at least some numeric values
        List<String> numericColumns = columns.stream()
                .filter(col -> !parseNumericValues(col).isEmpty())
                .collect(Collectors.toList());

        if (numericColumns.isEmpty()) {
            System.out.println("No numeric columns found.");
            return;
        }

        // Print header
        String headerFormat = "%-12s %8s %12s %12s %12s %12s %12s %12s %12s%n";
        System.out.printf(headerFormat, "Column", "Count", "Mean", "Std Dev", "Min", "25%", "Median", "75%", "Max");
        System.out.println("-".repeat(116));

        String rowFormat = "%-12s %8d %12.2f %12.2f %12.2f %12.2f %12.2f %12.2f %12.2f%n";

        for (String col : numericColumns) {
            List<Double> values = parseNumericValues(col);
            Collections.sort(values);

            double count  = values.size();
            double mean   = mean(values);
            double std    = standardDeviation(values, mean);
            double min    = values.get(0);
            double p25    = percentile(values, 0.25);
            double median = percentile(values, 0.50);
            double p75    = percentile(values, 0.75);
            double max    = values.get(values.size() - 1);

            System.out.printf(rowFormat, col, (int) count, mean, std, min, p25, median, p75, max);
        }
        System.out.println();
    }

    /**
     * Returns statistics for a single column as a Map — useful if you want to use the
     * values programmatically rather than just printing them.
     * e.g. Map<String, Double> stats = dataSet.describeColumn("expectedCashTurnover");
     *      double mean = stats.get("mean");
     */
    public Map<String, Double> describeColumn(String columnName) {
        assertColumnExists(columnName);

        List<Double> values = parseNumericValues(columnName);
        if (values.isEmpty())
            throw new IllegalArgumentException("Column '" + columnName + "' has no numeric values to describe.");

        Collections.sort(values);
        double mean = mean(values);

        Map<String, Double> stats = new LinkedHashMap<>(); // LinkedHashMap preserves insertion order
        stats.put("count",  (double) values.size());
        stats.put("mean",   mean);
        stats.put("std",    standardDeviation(values, mean));
        stats.put("min",    values.get(0));
        stats.put("25%",    percentile(values, 0.25));
        stats.put("50%",    percentile(values, 0.50));
        stats.put("75%",    percentile(values, 0.75));
        stats.put("max",    values.get(values.size() - 1));
        return stats;
    }

    // =====================================================================
    // INDIVIDUAL MATH OPERATIONS — use on any numeric column
    // =====================================================================

    /** Total number of non-null values in the column */
    public long count(String columnName) {
        assertColumnExists(columnName);
        return rows.stream()
                .map(row -> row.get(columnName))
                .filter(v -> v != null && !v.isBlank())
                .count();
    }

    /** Sum of all numeric values in the column */
    public double sum(String columnName) {
        return parseNumericValues(columnName).stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    /** Average of all numeric values in the column */
    public double mean(String columnName) {
        List<Double> values = parseNumericValues(columnName);
        if (values.isEmpty()) throw new ArithmeticException("No numeric values in column '" + columnName + "'");
        return mean(values);
    }

    /** Middle value of the column when sorted */
    public double median(String columnName) {
        List<Double> values = parseNumericValues(columnName);
        if (values.isEmpty()) throw new ArithmeticException("No numeric values in column '" + columnName + "'");
        Collections.sort(values);
        return percentile(values, 0.50);
    }

    /** Smallest value in the column */
    public double min(String columnName) {
        return parseNumericValues(columnName).stream()
                .mapToDouble(Double::doubleValue)
                .min()
                .orElseThrow(() -> new ArithmeticException("No numeric values in column '" + columnName + "'"));
    }

    /** Largest value in the column */
    public double max(String columnName) {
        return parseNumericValues(columnName).stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElseThrow(() -> new ArithmeticException("No numeric values in column '" + columnName + "'"));
    }

    /** How spread out the values are from the mean */
    public double standardDeviation(String columnName) {
        List<Double> values = parseNumericValues(columnName);
        if (values.isEmpty()) throw new ArithmeticException("No numeric values in column '" + columnName + "'");
        return standardDeviation(values, mean(values));
    }

    /** The difference between the 75th and 25th percentile — same as pandas IQR */
    public double interquartileRange(String columnName) {
        List<Double> values = parseNumericValues(columnName);
        Collections.sort(values);
        return percentile(values, 0.75) - percentile(values, 0.25);
    }

    // =====================================================================
    // PRIVATE HELPERS
    // =====================================================================

    private void assertColumnExists(String columnName) {
        if (!columns.contains(columnName))
            throw new IllegalArgumentException("Column '" + columnName + "' does not exist.");
    }

    /** Pulls all values from a column that can be parsed as a number, ignores the rest */
    private List<Double> parseNumericValues(String columnName) {
        return rows.stream()
                .map(row -> row.get(columnName))
                .filter(v -> v != null && !v.isBlank())
                .flatMap(v -> {
                    try { return java.util.stream.Stream.of(Double.parseDouble(v)); }
                    catch (NumberFormatException e) { return java.util.stream.Stream.empty(); }
                })
                .collect(Collectors.toList());
    }

    private double mean(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private double standardDeviation(List<Double> values, double mean) {
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }

    private double percentile(List<Double> sorted, double percentile) {
        double index = percentile * (sorted.size() - 1);
        int lower = (int) Math.floor(index);
        int upper = (int) Math.ceil(index);
        if (lower == upper) return sorted.get(lower);
        return sorted.get(lower) + (index - lower) * (sorted.get(upper) - sorted.get(lower));
    }

    @Override
    public String toString() {
        return "DataSet{source='" + source + "', columns=" + columns + ", rowCount=" + rowCount() + "}";
    }
}