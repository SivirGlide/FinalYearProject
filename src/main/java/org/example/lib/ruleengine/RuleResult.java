package org.example.lib.ruleengine;

import java.util.Optional;

public class RuleResult {

    private final String moduleName;
    private final Object value;
    private final boolean success;
    private final String errorMessage;

    public RuleResult(String moduleName, Object value) {
        this.moduleName   = moduleName;
        this.value        = value;
        this.success      = true;
        this.errorMessage = null;
    }

    public RuleResult(String moduleName, String errorMessage) {
        this.moduleName   = moduleName;
        this.value        = null;
        this.success      = false;
        this.errorMessage = errorMessage;
    }

    public String  getModuleName()  { return moduleName;   }
    public boolean isSuccess()      { return success;      }
    public String  getErrorMessage(){ return errorMessage; }

    public <T> Optional<T> getValue(Class<T> type) {
        if (!success || value == null) return Optional.empty();
        if (!type.isInstance(value))   return Optional.empty();
        return Optional.of(type.cast(value));
    }

    @Override
    public String toString() {
        if (!success) return String.format("RuleResult[%s] FAILED: %s", moduleName, errorMessage);
        return String.format("RuleResult[%s] = %s", moduleName, value);
    }
}
