package org.example.domain.customer.exporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.domain.customer.reader.DataSet;

import java.io.File;
import java.util.List;
import java.util.Map;

public class JsonExporter implements FileExporter {

    // INDENT_OUTPUT makes the JSON human-readable with proper formatting
    private final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    public void exportFile(DataSet dataSet, String filePath) throws Exception {
        List<Map<String, String>> rows = dataSet.getAllRows();
        mapper.writeValue(new File(filePath), rows);
        System.out.println("Exported " + dataSet.rowCount() + " rows to " + filePath);
    }
}
