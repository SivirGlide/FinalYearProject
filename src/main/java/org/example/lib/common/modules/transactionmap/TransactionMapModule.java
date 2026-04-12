package org.example.lib.common.modules.transactionmap;

import org.dflib.DataFrame;

/**
 * The contract every TransactionMap module must fulfil.
 *
 * WHY GENERIC?
 * Different modules produce fundamentally different outputs:
 *   - An address module might return a String (the average location name)
 *   - A frequency module might return a Map<String, Integer> (country → count)
 *   - A risk module might return a Double (a calculated score)
 *
 * Making this interface generic — TransactionMapModule<T> — lets each module
 * declare exactly what type it returns. The caller gets that type back directly
 * from ModuleResult without casting. If it weren't generic, every module would
 * have to return Object and the caller would have to cast blindly.
 *
 * IMPLEMENTING A MODULE:
 *
 *   public class TopBeneficiaryModule implements TransactionMapModule<String> {
 *
 *       @Override
 *       public String getModuleName() {
 *           return "TopBeneficiary";
 *       }
 *
 *       @Override
 *       public String run(DataFrame df) {
 *           // inspect df, compute result, return it
 *           return "Jane Smith";
 *       }
 *   }
 *
 * The module then gets added to a TransactionMap and its result is retrievable by name:
 *
 *   result.get("TopBeneficiary", String.class)  →  Optional<String>
 *
 * IMPORTANT:
 * Modules must never modify the DataFrame. They are read-only processors.
 * The same DataFrame instance is passed to every module in the map.
 *
 * @param <T> The type this module produces — declared by the implementing class.
 */
public interface TransactionMapModule<T> {

    /**
     * A unique name for this module.
     *
     * This is used as the key when storing and retrieving results from
     * TransactionMapResult. If two modules share the same name, the second
     * one's result will overwrite the first — so names must be unique within
     * a TransactionMap.
     *
     * Convention: use PascalCase, e.g. "AverageLocation", "TopBeneficiary".
     */
    String getModuleName();

    /**
     * Processes the DataFrame and returns this module's result.
     *
     * @param df  The customer's transaction DataFrame. Never modify this.
     * @return    The computed result — whatever type T this module declares.
     */
    T run(DataFrame df);
}
