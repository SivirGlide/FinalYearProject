package org.example.lib.ruleengine;

import org.dflib.DataFrame;
import org.example.lib.common.modules.ruleengine.RuleEngineModule;
import org.example.lib.transactionmapper.TransactionMapResult;

import java.util.ArrayList;
import java.util.List;

public class RuleEngine {

    private final DataFrame transaction;
    private final DataFrame customerProfile;
    private final TransactionMapResult transactionMap;

    private final List<RuleEngineModule<?>> modules = new ArrayList<>();

    public RuleEngine(DataFrame transaction,
                      DataFrame customerProfile,
                      TransactionMapResult transactionMap) {
        if (transaction    == null) throw new IllegalArgumentException("Transaction must not be null.");
        if (customerProfile == null) throw new IllegalArgumentException("CustomerProfile must not be null.");
        if (transactionMap  == null) throw new IllegalArgumentException("TransactionMapResult must not be null.");
        this.transaction     = transaction;
        this.customerProfile = customerProfile;
        this.transactionMap  = transactionMap;
    }

    public RuleEngine addModule(RuleEngineModule<?> module) {
        if (module == null) throw new IllegalArgumentException("Module must not be null.");
        modules.add(module);
        return this;
    }

    public RuleEngineResult run() {
        RuleEngineResult result = new RuleEngineResult();

        for (RuleEngineModule<?> module : modules) {
            String name = module.getModuleName();
            try {
                Object value = module.run(transaction, customerProfile, transactionMap);
                result.put(new RuleResult(name, value));
            } catch (Exception e) {
                result.put(new RuleResult(name,
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