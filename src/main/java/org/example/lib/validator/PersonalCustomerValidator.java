package org.example.lib.validator;

import org.dflib.DataFrame;
import org.example.lib.common.schemas.PersonalCustomerSchema;
import org.example.lib.validator.report.ValidationReport;

public class PersonalCustomerValidator implements DataValidator{

    public PersonalCustomerValidator() {}

    // The schema instance is created once when the class is first loaded and reused for every validate() call.
    private static final PersonalCustomerSchema SCHEMA = new PersonalCustomerSchema();

    /**
     * Validates a DFLib DataFrame against the Personal Customer schema.
     */
    @Override
    public ValidationReport validate(DataFrame df) {
        ValidationReport report = new SchemaValidator(SCHEMA).validate(df);
        return this.CustomValidation(report, df);
    }

    protected ValidationReport CustomValidation(ValidationReport report, DataFrame df) {
        return report;
    }

}
