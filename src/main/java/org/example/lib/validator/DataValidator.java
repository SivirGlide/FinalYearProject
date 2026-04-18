package org.example.lib.validator;

import org.dflib.DataFrame;
import org.example.lib.validator.report.ValidationReport;

public interface DataValidator {
    ValidationReport validate(DataFrame df);
}
