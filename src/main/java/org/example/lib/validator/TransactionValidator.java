package org.example.lib.validator;
import org.dflib.DataFrame;
import org.example.lib.common.schemas.TransactionSchema;

public class TransactionValidator {

    private TransactionValidator() {}

    private static final TransactionSchema SCHEMA = new TransactionSchema();

    public static ValidationReport validate(DataFrame df) {
        return new SchemaValidator(SCHEMA).validate(df);
    }
}
