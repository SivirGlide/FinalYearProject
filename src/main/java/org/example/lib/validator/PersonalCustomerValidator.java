package org.example.lib.validator;

import org.dflib.DataFrame;
import org.example.lib.common.schemas.PersonalCustomerSchema;

public class PersonalCustomerValidator {

    private PersonalCustomerValidator() {}

    // The schema instance is created once when the class is first loaded and reused for every validate() call.
    private static final PersonalCustomerSchema SCHEMA = new PersonalCustomerSchema();

    /**
     * Validates a DFLib DataFrame against the Personal Customer schema.
     */
    public static ValidationReport validate(DataFrame df) {
        return new SchemaValidator(SCHEMA).validate(df);
    }
}
