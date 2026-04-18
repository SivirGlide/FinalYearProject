package org.example.lib.validator;

import org.dflib.DataFrame;
import org.example.lib.common.schemas.BusinessCustomerSchema;
import org.example.lib.validator.report.ValidationReport;

public class BusinessCustomerValidator implements DataValidator{

    public BusinessCustomerValidator() {}

    // The schema instance is created once when the class is first loaded and reused for every validate() call.
    private static final BusinessCustomerSchema SCHEMA = new BusinessCustomerSchema();

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
