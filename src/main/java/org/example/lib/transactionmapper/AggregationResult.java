package org.example.lib.transactionmapper;

import org.dflib.DataFrame;

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