package org.example.lib.transactionmapper;

/**
 * Protocol type for TransactionMapModules that return derived information as JSON.
 * Modules are not required to use this, but SHOULD when their output is a
 * set of facts or metrics derived from the incoming transaction DataFrame.
 */
public class InformativeResult {

    private final String json;

    public InformativeResult(String json) {
        if (json == null) throw new IllegalArgumentException("JSON must not be null.");
        this.json = json;
    }

    public String getJson() {
        return json;
    }
}