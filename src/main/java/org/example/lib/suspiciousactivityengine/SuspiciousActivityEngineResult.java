package org.example.lib.suspiciousactivityengine;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SuspiciousActivityEngineResult {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final List<HashMap<String, Object>> results = new ArrayList<>();

    void add(HashMap<String, Object> moduleOutput) {
        results.add(moduleOutput);
    }

    public List<HashMap<String, Object>> all() {
        return Collections.unmodifiableList(results);
    }

    public int size() {
        return results.size();
    }

    public String toJson() {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(results);
        } catch (Exception e) {
            return "{\"error\": \"Failed to serialise results: " + e.getMessage() + "\"}";
        }
    }
}