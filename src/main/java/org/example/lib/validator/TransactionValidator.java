package org.example.lib.validator;
import org.dflib.DataFrame;
import org.example.lib.common.schemas.TransactionSchema;
import org.example.lib.validator.report.ValidationReport;

public class TransactionValidator implements DataValidator{

    public TransactionValidator() {}

    private static final TransactionSchema SCHEMA = new TransactionSchema();

    @Override
    public ValidationReport validate(DataFrame df) {
        ValidationReport report = new SchemaValidator(SCHEMA).validate(df);
        return this.CustomValidation(report, df);
    }

    protected ValidationReport CustomValidation(ValidationReport report, DataFrame df) {
        return null;
    }
}
