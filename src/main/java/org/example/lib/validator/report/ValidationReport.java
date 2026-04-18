package org.example.lib.validator.report;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class ValidationReport {

    private final String schemaName;          // e.g. "PersonalCustomerSchema"
    private final long rowsInspected;
    private final List<ValidationIssue> issues = new ArrayList<>();

    public ValidationReport(String schemaName, long rowsInspected) {
        this.schemaName      = schemaName;
        this.rowsInspected   = rowsInspected;
    }

    public void addIssue(ValidationIssue issue) {
        issues.add(issue);
    }

    public boolean hasErrors() {
        return issues.stream().anyMatch(i -> i.getSeverity() == ValidationIssue.Severity.ERROR);
    }

    public boolean hasWarnings() {
        return issues.stream().anyMatch(i -> i.getSeverity() == ValidationIssue.Severity.WARNING);
    }

    public List<ValidationIssue> getAllIssues() {
        return List.copyOf(issues);
    }

    /** Only issues at or above a given severity */
    public List<ValidationIssue> getIssues(ValidationIssue.Severity severity) {
        return issues.stream()
                .filter(i -> i.getSeverity() == severity)
                .collect(Collectors.toList());
    }

    public List<ValidationIssue> getIssuesByColumn(String columnName) {
        return issues.stream()
                .filter(i -> i.getColumnName().equalsIgnoreCase(columnName))
                .collect(Collectors.toList());
    }
}
