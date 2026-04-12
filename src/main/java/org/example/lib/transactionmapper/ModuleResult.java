package org.example.lib.transactionmapper;

import java.util.Optional;

/**
 * Wraps the output produced by a single module after it has run.
 *
 * WHY WRAP THE RESULT AT ALL?
 * When TransactionMap runs all its modules it needs to store their results
 * in a single collection — but each module returns a different type.
 * The only way to store mixed types in one collection is to store them as
 * Object internally. ModuleResult hides that Object and exposes a typed
 * getter using generics, so the caller never has to write an unsafe cast.
 *
 * INTERNAL vs EXTERNAL TYPES:
 *   Internally  → the value is stored as Object (unavoidable for mixed types)
 *   Externally  → getValue(Class<T>) asks the caller to declare what type
 *                 they expect, and the method casts safely using Class.cast()
 *                 which throws ClassCastException if the types don't match,
 *                 rather than silently doing the wrong thing.
 *
 * EXAMPLE:
 *   ModuleResult result = ...;
 *   Optional<String> name = result.getValue(String.class);
 */
public class ModuleResult {

    private final String moduleName;
    private final Object value;       // Stored as Object to allow any return type
    private final boolean success;    // False if the module threw an exception
    private final String errorMessage;

    /** Constructor for a successful result */
    public ModuleResult(String moduleName, Object value) {
        this.moduleName   = moduleName;
        this.value        = value;
        this.success      = true;
        this.errorMessage = null;
    }

    /** Constructor for a failed result — module threw an exception */
    public ModuleResult(String moduleName, String errorMessage) {
        this.moduleName   = moduleName;
        this.value        = null;
        this.success      = false;
        this.errorMessage = errorMessage;
    }

    public String  getModuleName()  { return moduleName;   }
    public boolean isSuccess()      { return success;      }
    public String  getErrorMessage(){ return errorMessage; }

    /**
     * Returns the result value cast to the requested type, or empty if:
     *   - The module failed (threw an exception during run)
     *   - The value is null
     *   - The value is not an instance of the requested type
     *
     * Using Optional here means the caller is forced to handle the "not present"
     * case explicitly — it's impossible to accidentally call methods on a null.
     *
     * @param type  The Class object for the expected type, e.g. String.class
     * @param <T>   The expected type
     */
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
