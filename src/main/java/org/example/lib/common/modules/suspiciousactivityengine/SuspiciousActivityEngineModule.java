package org.example.lib.common.modules.suspiciousactivityengine;

import org.dflib.DataFrame;

import java.util.HashMap;

public interface SuspiciousActivityEngineModule {

    String getModuleName();

    HashMap<String, Object> run(DataFrame transaction,
                                DataFrame customerProfile,
                                Object transactionMapResult);
}
