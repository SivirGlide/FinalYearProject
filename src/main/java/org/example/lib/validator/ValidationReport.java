package org.example.lib.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Collects every ValidationIssue found during a validation run and provides
 * helper methods to query the results.
 *
 * After calling SchemaValidator.validate(...) you get back one of these.
 * You can then ask things like:
 *   report.hasErrors()          → did anything fail hard?
 *   report.getIssuesByColumn()  → what's wrong with a specific column?
 *   report.printSummary()       → print a formatted report to console
 */
public class ValidationReport {

    private final String schemaName;          // e.g. "PersonalCustomerSchema"
    private final long rowsInspected;
    private final List<ValidationIssue> issues = new ArrayList<>();

    public ValidationReport(String schemaName, long rowsInspected) {
        this.schemaName      = schemaName;
        this.rowsInspected   = rowsInspected;
    }

    /** Add a new issue — called by SchemaValidator during validation */
    public void addIssue(ValidationIssue issue) {
        issues.add(issue);
    }

    // -------------------------------------------------------------------------
    // Query helpers
    // -------------------------------------------------------------------------

    /** True if there is at least one ERROR-level issue */
    public boolean hasErrors() {
        return issues.stream().anyMatch(i -> i.getSeverity() == ValidationIssue.Severity.ERROR);
    }

    /** True if there is at least one WARNING-level issue */
    public boolean hasWarnings() {
        return issues.stream().anyMatch(i -> i.getSeverity() == ValidationIssue.Severity.WARNING);
    }

    /** All issues regardless of severity */
    public List<ValidationIssue> getAllIssues() {
        return List.copyOf(issues);
    }

    /** Only issues at or above a given severity */
    public List<ValidationIssue> getIssues(ValidationIssue.Severity severity) {
        return issues.stream()
                .filter(i -> i.getSeverity() == severity)
                .collect(Collectors.toList());
    }

    /** All issues related to a specific column name */
    public List<ValidationIssue> getIssuesByColumn(String columnName) {
        return issues.stream()
                .filter(i -> i.getColumnName().equalsIgnoreCase(columnName))
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Reporting
    // -------------------------------------------------------------------------

    /**
     * Prints a nicely formatted summary to System.out.
     *
     * Typical output:
     * ═══════════════════════════════════════════
     *  VALIDATION REPORT — PersonalCustomerSchema
     *  Rows inspected : 1500
     *  Errors         : 2
     *  Warnings       : 5
     * ═══════════════════════════════════════════
     * ERROR    [Column: Date Of Birth              ] Column is missing from DataFrame
     * WARNING  [Column: Nationality                | Row: 42] Value "USA" is not a 2-char ISO code
     * ...
     */
    public void printSummary() {
        long errors   = issues.stream().filter(i -> i.getSeverity() == ValidationIssue.Severity.ERROR).count();
        long warnings = issues.stream().filter(i -> i.getSeverity() == ValidationIssue.Severity.WARNING).count();
        long infos    = issues.stream().filter(i -> i.getSeverity() == ValidationIssue.Severity.INFO).count();

        String border = "═".repeat(70);

        System.out.println(border);
        System.out.printf(" VALIDATION REPORT — %s%n", schemaName);
        System.out.printf(" Rows inspected : %d%n", rowsInspected);
        System.out.printf(" Errors         : %d%n", errors);
        System.out.printf(" Warnings       : %d%n", warnings);
        System.out.printf(" Info           : %d%n", infos);
        System.out.println(border);

        if (issues.isEmpty()) {
            System.out.println(" ✓ No issues found. DataFrame looks valid.");
        } else {
            // Print ERRORs first, then WARNINGs, then INFOs
            for (ValidationIssue.Severity sev : ValidationIssue.Severity.values()) {
                List<ValidationIssue> group = getIssues(sev);
                if (!group.isEmpty()) {
                    System.out.println();
                    System.out.println(" ── " + sev + "S ──────────────────────────────────────────────────────");
                    group.forEach(i -> System.out.println(" " + i));
                }
            }
        }

        System.out.println(border);
    }
}
