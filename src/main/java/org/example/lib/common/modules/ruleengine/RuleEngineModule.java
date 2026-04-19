package org.example.lib.common.modules.ruleengine;

import org.dflib.DataFrame;
import org.example.lib.transactionmapper.TransactionMapResult;

public interface RuleEngineModule<T> {

    String getModuleName();

    T run(DataFrame transaction,
          DataFrame customerProfile,
          TransactionMapResult transactionMap);
}
