package org.example.lib.transactionmapper;

import org.dflib.DataFrame;

import java.util.ArrayList;
import java.util.List;

public class TransactionMap {

    private final DataFrame df;

    // Wildcard <?> here because each module can have a different type parameter.
    // We don't care what T is at this level — we just call run() on each module
    // and store whatever it returns. The type safety is handled inside ModuleResult.
    private final List<TransactionMapModule<?>> modules = new ArrayList<>();

    /**
     * Creates a TransactionMap for a single customer's transaction DataFrame.
     *
     * @param df  The transaction DataFrame. Must not be null. Never modified.
     */
    public TransactionMap(DataFrame df) {
        if (df == null) throw new IllegalArgumentException("DataFrame must not be null.");
        this.df = df;
    }

    /**
     * Registers a module to be run against this map's DataFrame.
     *
     * Returns 'this' so calls can be chained:
     *   map.addModule(new ModuleA()).addModule(new ModuleB())
     *
     * Modules run in the order they are added.
     *
     * @param module  The module to add. Must not be null.
     * @return        This TransactionMap, for chaining.
     */
    public TransactionMap addModule(TransactionMapModule<?> module) {
        if (module == null) throw new IllegalArgumentException("Module must not be null.");
        modules.add(module);
        return this;
    }

    /**
     * Runs all registered modules against the DataFrame and returns the results.
     *
     * Each module receives the same DataFrame. Modules are run in registration order.
     * If a module throws any exception, the error is captured as a failed ModuleResult
     * and execution continues with the next module — a single failure is isolated.
     *
     * @return  A TransactionMapResult containing one entry per registered module.
     */
    public TransactionMapResult run() {
        TransactionMapResult result = new TransactionMapResult();

        for (TransactionMapModule<?> module : modules) {
            String name = module.getModuleName();
            try {
                Object value = module.run(df);
                result.put(new ModuleResult(name, value));
            } catch (Exception e) {
                // Isolate the failure — record it and move on to the next module.
                // This means a NullPointerException in one module won't silently
                // prevent the remaining modules from running.
                result.put(new ModuleResult(name,
                        String.format("Module threw %s: %s",
                                e.getClass().getSimpleName(), e.getMessage())));
            }
        }

        return result;
    }

    /** Returns how many modules are currently registered */
    public int moduleCount() {
        return modules.size();
    }
}
