package org.example.domain.source.reader;

import org.example.domain.source.interfaces.DataReader;

import java.io.*;
import java.util.*;

public class CSVReader implements DataReader {
    @Override
    public Map<String, List<String>> readMap(String source) throws IOException {
        Map<String, List<String>> data = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(source));
        String line;
        boolean isHeader = true;

        while ((line = reader.readLine()) != null) {
            if (isHeader) {
                isHeader = false;
                continue;
            }

            String[] columns = line.split(",");
            String key = columns[0]; // use first column as key

            List<String> values = new ArrayList<>(Arrays.asList(columns).subList(1, columns.length));

            data.put(key, values);
        }

        reader.close();
        return data;
    };
}
