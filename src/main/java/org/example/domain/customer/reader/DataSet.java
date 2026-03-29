package org.example.domain.customer.reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stores imported data in a structure similar to a pandas DataFrame.
 * Columns are the headers, rows are each record as a Map of column -> value.
 */
public class DataSet {

    private final String source;         // e.g. "customers.csv"
    private final List<String> columns;  // the headers/field names
    private final List<Map<String, String>> rows; // each row is a column->value map

    public DataSet(String source, List<String> columns, List<Map<String, String>> rows) {
        this.source = source;
        this.columns = columns;
        this.rows = rows;
    }

    // --- Access methods (similar to pandas operations) ---

    /** Total number of rows */
    public int rowCount() {
        return rows.size();
    }

    /** All column names */
    public List<String> getColumns() {
        return columns;
    }

    /** Get a single row by its index position */
    public Map<String, String> getRow(int index) {
        if (index < 0 || index >= rows.size()) {
            throw new IndexOutOfBoundsException("Row index " + index + " is out of bounds. Total rows: " + rows.size());
        }
        return rows.get(index);
    }

    /** Get all rows */
    public List<Map<String, String>> getAllRows() {
        return rows;
    }

    /** Get a specific cell value — like df['column'][rowIndex] in pandas */
    public Optional<String> getValue(int rowIndex, String column) {
        return Optional.ofNullable(getRow(rowIndex).get(column));
    }

    /** Get all values from a single column — like df['column'] in pandas */
    public List<String> getColumn(String columnName) {
        if (!columns.contains(columnName)) {
            throw new IllegalArgumentException("Column '" + columnName + "' does not exist.");
        }
        List<String> values = new ArrayList<>();
        for (Map<String, String> row : rows) {
            values.add(row.get(columnName));
        }
        return values;
    }

    /** Check if the dataset has a specific column */
    public boolean hasColumn(String columnName) {
        return columns.contains(columnName);
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "DataSet{source='" + source + "', columns=" + columns + ", rowCount=" + rowCount() + "}";
    }
}
