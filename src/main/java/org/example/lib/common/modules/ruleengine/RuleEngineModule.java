package org.example.lib.common.modules.ruleengine;

import org.dflib.DataFrame;

import java.util.HashMap;

public interface RuleEngineModule {

    String getModuleName();

    HashMap<String, Object> run(DataFrame transaction,
                                DataFrame customerProfile,
                                Object transactionMapResult);
}
