package org.example.lib.transactionmapper;

import org.dflib.DataFrame;
import org.example.lib.common.modules.transactionmap.TransactionMapModule;

import java.util.ArrayList;
import java.util.List;

public class TransactionMap {

    private final DataFrame df;

    private final List<TransactionMapModule<?>> modules = new ArrayList<>();

    public TransactionMap(DataFrame df) {
        if (df == null) throw new IllegalArgumentException("DataFrame must not be null.");
        this.df = df;
    }

    public TransactionMap addModule(TransactionMapModule<?> module) {
        if (module == null) throw new IllegalArgumentException("Module must not be null.");
        modules.add(module);
        return this;
    }

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

    public int moduleCount() {
        return modules.size();
    }
}
