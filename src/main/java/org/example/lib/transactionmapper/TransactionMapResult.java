package org.example.lib.transactionmapper;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Holds the results produced by every module after TransactionMap.run() completes.
 *
 * Internally this is just a Map<String, ModuleResult> keyed by module name.
 * The methods here are convenience wrappers so the caller doesn't have to
 * interact with the map directly.
 *
 * USAGE EXAMPLE:
 *
 *   TransactionMapResult results = map.run();
 *
 *   // Retrieve a typed result by module name
 *   Optional<String> location = results.get("AverageLocation", String.class);
 *   location.ifPresent(loc -> System.out.println("Customer transacts around: " + loc));
 *
 *   // Check if a specific module ran successfully
 *   if (results.isSuccessful("TopBeneficiary")) { ... }
 *
 *   // Iterate over everything
 *   results.all().forEach(r -> System.out.println(r));
 *
 * WHY LinkedHashMap?
 * A regular HashMap stores entries in unpredictable order. LinkedHashMap preserves
 * insertion order, so results are printed/iterated in the same order the modules
 * were added to the TransactionMap — which is easier to reason about.
 */
public class TransactionMapResult {

    private final Map<String, ModuleResult> results = new LinkedHashMap<>();

    /** Called by TransactionMap to store each module's result after it runs */
    void put(ModuleResult result) {
        results.put(result.getModuleName(), result);
    }

    /**
     * Returns the typed result for a module by name.
     *
     * Returns Optional.empty() if:
     *   - No module with that name was registered
     *   - The module failed during execution
     *   - The result is not of the expected type
     *
     * @param moduleName  The name returned by TransactionMapModule.getModuleName()
     * @param type        The expected result type, e.g. String.class, Double.class
     */
    public <T> Optional<T> get(String moduleName, Class<T> type) {
        ModuleResult result = results.get(moduleName);
        if (result == null) return Optional.empty();
        return result.getValue(type);
    }

    /**
     * Returns the raw ModuleResult for a module, including failure info if it failed.
     * Useful when you want to inspect whether the module succeeded or read the error.
     */
    public Optional<ModuleResult> getRaw(String moduleName) {
        return Optional.ofNullable(results.get(moduleName));
    }

    /** Returns true if the named module ran and completed without throwing an exception */
    public boolean isSuccessful(String moduleName) {
        ModuleResult result = results.get(moduleName);
        return result != null && result.isSuccess();
    }

    /** Returns all ModuleResults in the order the modules were added */
    public Collection<ModuleResult> all() {
        return Collections.unmodifiableCollection(results.values());
    }

    /** Returns how many modules produced results */
    public int size() {
        return results.size();
    }

    /** Prints a summary of every module result to System.out */
    public void printSummary() {
        System.out.println("═".repeat(60));
        System.out.println(" TRANSACTION MAP RESULTS");
        System.out.printf(" Modules run: %d%n", results.size());
        System.out.println("═".repeat(60));
        results.values().forEach(r -> System.out.println(" " + r));
        System.out.println("═".repeat(60));
    }
}
