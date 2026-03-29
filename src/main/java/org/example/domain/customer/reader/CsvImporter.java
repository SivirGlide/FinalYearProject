package org.example.domain.customer.reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class CsvImporter implements FileImporter {

    private final char delimiter;

    // Default comma delimiter
    public CsvImporter() {
        this.delimiter = ',';
    }

    // Flexible — pass in ';' or '\t' for other CSV variants
    public CsvImporter(char delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public DataSet importFile(String filePath) throws Exception {
        List<String> columns = new ArrayList<>();
        List<Map<String, String>> rows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();

            if (headerLine == null) {
                throw new IllegalArgumentException("CSV file is empty: " + filePath);
            }

            // First line is the headers/column names
            columns = Arrays.asList(headerLine.split(String.valueOf(delimiter)));

            String line;
            int lineNumber = 2; // for useful error messages

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue; // skip empty lines

                String[] values = line.split(String.valueOf(delimiter), -1); // -1 keeps trailing empty strings

                if (values.length != columns.size()) {
                    throw new IllegalStateException(
                            "Row " + lineNumber + " has " + values.length + " values but there are " + columns.size() + " columns."
                    );
                }

                Map<String, String> row = new LinkedHashMap<>(); // LinkedHashMap preserves column order
                for (int i = 0; i < columns.size(); i++) {
                    row.put(columns.get(i).trim(), values[i].trim());
                }
                rows.add(row);
                lineNumber++;
            }
        }

        return new DataSet(filePath, columns, rows);
    }
}
