package org.example.lib.ruleengine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class RuleEngineResult {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Map<String, RuleResult> results = new LinkedHashMap<>();

    void put(RuleResult result) {
        results.put(result.getModuleName(), result);
    }

    public <T> Optional<T> get(String moduleName, Class<T> type) {
        RuleResult result = results.get(moduleName);
        if (result == null) return Optional.empty();
        return result.getValue(type);
    }

    public Optional<RuleResult> getRaw(String moduleName) {
        return Optional.ofNullable(results.get(moduleName));
    }

    public boolean isSuccessful(String moduleName) {
        RuleResult result = results.get(moduleName);
        return result != null && result.isSuccess();
    }

    public Collection<RuleResult> all() {
        return Collections.unmodifiableCollection(results.values());
    }

    public int size() {
        return results.size();
    }

    public String toJson() {
        ObjectNode root = MAPPER.createObjectNode();

        for (RuleResult r : results.values()) {
            ObjectNode entry = root.putObject(r.getModuleName());
            entry.put("success", r.isSuccess());
            if (r.isSuccess()) {
                entry.putPOJO("value", r.getValue(Object.class).orElse(null));
            } else {
                entry.put("error", r.getErrorMessage());
            }
        }

        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            return "{\"error\": \"Failed to serialise results: " + e.getMessage() + "\"}";
        }
    }
}
