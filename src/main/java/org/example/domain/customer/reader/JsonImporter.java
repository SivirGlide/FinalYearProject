package org.example.domain.customer.reader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.*;

public class JsonImporter implements FileImporter {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public DataSet importFile(String filePath) throws Exception {
        JsonNode root = mapper.readTree(new File(filePath));

        if (!root.isArray()) {
            throw new IllegalArgumentException("JSON file must contain an array of objects. Got: " + root.getNodeType());
        }

        if (root.isEmpty()) {
            throw new IllegalArgumentException("JSON file is empty: " + filePath);
        }

        // Pull column names from the first object's keys
        List<String> columns = new ArrayList<>();
        root.get(0).fieldNames().forEachRemaining(columns::add);

        List<Map<String, String>> rows = new ArrayList<>();

        for (JsonNode node : root) {
            Map<String, String> row = new LinkedHashMap<>();
            for (String column : columns) {
                // asText() safely converts any JSON value (number, boolean, string) to a String
                row.put(column, node.has(column) ? node.get(column).asText() : null);
            }
            rows.add(row);
        }

        return new DataSet(filePath, columns, rows);
    }
}
