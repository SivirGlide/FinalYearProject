package org.example.lib.common.modules.transactionmap;

import org.dflib.DataFrame;

/**
 * Protocol type for TransactionMapModules that return an aggregated DataFrame.
 * Modules are not required to use this, but SHOULD when their output is a
 * summary/aggregation of the incoming transaction DataFrame.
 */
public class AggregationResult {

    private final DataFrame data;

    public AggregationResult(DataFrame data) {
        if (data == null) throw new IllegalArgumentException("DataFrame must not be null.");
        this.data = data;
    }

    public DataFrame getData() {
        return data;
    }
}