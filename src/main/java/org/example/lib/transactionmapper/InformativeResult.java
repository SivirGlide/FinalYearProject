package org.example.lib.transactionmapper;

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