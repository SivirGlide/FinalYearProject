package org.example.lib.ruleengine;

import org.dflib.DataFrame;
import org.example.lib.common.modules.ruleengine.RuleEngineModule;
import org.example.lib.transactionmapper.TransactionMapResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RuleEngine {

    private final DataFrame transaction;
    private final DataFrame customerProfile;
    private final TransactionMapResult transactionMap;

    private final List<RuleEngineModule> modules = new ArrayList<>();

    public RuleEngine(DataFrame transaction,
                      DataFrame customerProfile,
                      TransactionMapResult transactionMap) {
        this.transaction     = transaction;
        this.customerProfile = customerProfile;
        this.transactionMap  = transactionMap;
    }

    public RuleEngine addModule(RuleEngineModule module) {
        if (module == null) throw new IllegalArgumentException("Module must not be null.");
        modules.add(module);
        return this;
    }

    public RuleEngineResult run() {
        RuleEngineResult result = new RuleEngineResult();

        for (RuleEngineModule module : modules) {
            String name = module.getModuleName();
            try {
                HashMap<String, Object> output = module.run(transaction, customerProfile, transactionMap);
                result.add(output);
            } catch (Exception e) {
                HashMap<String, Object> errorOutput = new HashMap<>();
                errorOutput.put("Module Name", name);
                errorOutput.put("Module Ran", false);
                errorOutput.put("Risk Score", -1);
                errorOutput.put("Comments", String.format("Module threw %s: %s",
                        e.getClass().getSimpleName(), e.getMessage()));
                result.add(errorOutput);
            }
        }

        return result;
    }

    public int moduleCount() {
        return modules.size();
    }
}