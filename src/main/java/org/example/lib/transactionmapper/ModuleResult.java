package org.example.lib.transactionmapper;

import java.util.Optional;

public class ModuleResult {

    private final String moduleName;
    private final Object value;       //to allow any return type
    private final boolean success;    // False if the module threw an exception
    private final String errorMessage;

    // Constructor for a successful result
    public ModuleResult(String moduleName, Object value) {
        this.moduleName   = moduleName;
        this.value        = value;
        this.success      = true;
        this.errorMessage = null;
    }

    // Constructor for a failed result — module threw an exception
    public ModuleResult(String moduleName, String errorMessage) {
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
        if (!success) {
            return String.format("ModuleResult[%s] FAILED: %s", moduleName, errorMessage);
        }
        return String.format("ModuleResult[%s] = %s", moduleName, value);
    }
}
