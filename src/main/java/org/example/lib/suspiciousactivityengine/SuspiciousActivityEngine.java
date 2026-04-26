package org.example.lib.suspiciousactivityengine;

import org.dflib.DataFrame;
import org.example.lib.common.modules.suspiciousactivityengine.SuspiciousActivityEngineModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SuspiciousActivityEngine {

    private final DataFrame transaction;
    private final DataFrame customerProfile;
    private final Object transactionMapResult;

    private final List<SuspiciousActivityEngineModule> modules = new ArrayList<>();

    public SuspiciousActivityEngine(DataFrame transaction,
                                    DataFrame customerProfile,
                                    Object transactionMapResult) {
        this.transaction     = transaction;
        this.customerProfile = customerProfile;
        this.transactionMapResult  = transactionMapResult;
    }

    public SuspiciousActivityEngine addModule(SuspiciousActivityEngineModule module) {
        if (module == null) throw new IllegalArgumentException("Module must not be null.");
        modules.add(module);
        return this;
    }

    public SuspiciousActivityEngineResult run() {
        SuspiciousActivityEngineResult result = new SuspiciousActivityEngineResult();

        for (SuspiciousActivityEngineModule module : modules) {
            String name = module.getModuleName();
            try {
                HashMap<String, Object> output = module.run(transaction, customerProfile, transactionMapResult);
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