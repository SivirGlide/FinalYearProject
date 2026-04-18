package org.example.lib.common.modules.transactionmap;

import org.dflib.DataFrame;

public interface TransactionMapModule<T> {

    String getModuleName();
    T run(DataFrame df);
}
