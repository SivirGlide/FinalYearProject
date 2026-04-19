package org.example.lib.transactionmapper;

import org.dflib.DataFrame;
import org.example.lib.common.modules.transactionmap.TransactionMapModule;

import java.util.ArrayList;
import java.util.HashMap;
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

    public HashMap<String, Object> run() {
        HashMap<String, Object> result = new HashMap<>();

        for (TransactionMapModule<?> module : modules) {
            String name = module.getModuleName();
            try {
                result.put(name, module.run(df));
            } catch (Exception e) {
                result.put(name, String.format("Module threw %s: %s",
                        e.getClass().getSimpleName(), e.getMessage()));
            }
        }

        return result;
    }

    public int moduleCount() {
        return modules.size();
    }
}
