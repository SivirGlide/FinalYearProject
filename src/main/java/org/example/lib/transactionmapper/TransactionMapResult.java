package org.example.lib.transactionmapper;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class TransactionMapResult {

    private final Map<String, ModuleResult> results = new LinkedHashMap<>();

    /** Called by TransactionMap to store each module's result after it runs */
    void put(ModuleResult result) {
        results.put(result.getModuleName(), result);
    }

    public <T> Optional<T> get(String moduleName, Class<T> type) {
        ModuleResult result = results.get(moduleName);
        if (result == null) return Optional.empty();
        return result.getValue(type);
    }

    public Optional<ModuleResult> getRaw(String moduleName) {
        return Optional.ofNullable(results.get(moduleName));
    }

    public boolean isSuccessful(String moduleName) {
        ModuleResult result = results.get(moduleName);
        return result != null && result.isSuccess();
    }

    public Collection<ModuleResult> all() {
        return Collections.unmodifiableCollection(results.values());
    }

    /** Returns how many modules produced results */
    public int size() {
        return results.size();
    }

    /** Prints a summary of every module result to System.out, this should be changed to a log to return */
    public void printSummary() {
        System.out.println("═".repeat(60));
        System.out.println(" TRANSACTION MAP RESULTS");
        System.out.printf(" Modules run: %d%n", results.size());
        System.out.println("═".repeat(60));
        results.values().forEach(r -> System.out.println(" " + r));
        System.out.println("═".repeat(60));
    }
}
