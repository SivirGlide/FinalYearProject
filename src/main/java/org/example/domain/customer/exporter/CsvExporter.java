package org.example.domain.customer.exporter;

import org.example.domain.customer.reader.DataSet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class CsvExporter implements FileExporter {

    private final char delimiter;

    public CsvExporter() {
        this.delimiter = ',';
    }

    public CsvExporter(char delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public void exportFile(DataSet dataSet, String filePath) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {

            // Write header row
            writer.write(String.join(String.valueOf(delimiter), dataSet.getColumns()));
            writer.newLine();

            // Write each row
            for (Map<String, String> row : dataSet.getAllRows()) {
                StringJoiner joiner = new StringJoiner(String.valueOf(delimiter));

                for (String column : dataSet.getColumns()) {
                    String value = row.getOrDefault(column, "");
                    // Wrap in quotes if the value contains the delimiter or a newline
                    if (value.contains(String.valueOf(delimiter)) || value.contains("\n")) {
                        value = "\"" + value + "\"";
                    }
                    joiner.add(value);
                }

                writer.write(joiner.toString());
                writer.newLine();
            }
        }

        System.out.println("Exported " + dataSet.rowCount() + " rows to " + filePath);
    }
}
