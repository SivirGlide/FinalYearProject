# Fraud Detection Library

A Java library for analysing customer transaction data and scoring transactions for fraud risk. Built on [DFLib](https://dflib.org/) for DataFrame operations and Jackson for JSON output.

---

## Architecture Overview

The library is built around three sequential layers:

```
Customer Transactions (DataFrame)
         │
         ▼
  ┌─────────────────┐
  │    Validator    │  ──── schema + domain checks, returns ValidationReport
  └─────────────────┘
         │
         ▼
  ┌─────────────────┐
  │  TransactionMap │  ──── runs analysis modules over the full transaction history
  └─────────────────┘
         │  HashMap<String, Object>
         ▼
  ┌──────────────────────────────┐
  │  SuspiciousActivityEngine    │  ──── scores a single new transaction against that analysis
  └──────────────────────────────┘
         │  SuspiciousActivityEngineResult (JSON)
         ▼
    Risk Assessment
```

---

## Package Structure

```
org.example.lib
├── common
│   ├── columns          — ColumnDefinition, ColumnType, StandardColumnType
│   ├── countries        — IsoCountryCodes
│   ├── modules
│   │   ├── transactionmap           — TransactionMapModule interface + built-in modules
│   │   └── suspiciousactivityengine — SuspiciousActivityEngineModule interface + built-in modules
│   └── schemas          — DataFrameSchema, TransactionSchema, PersonalCustomerSchema, BusinessCustomerSchema
├── transactionmapper    — TransactionMap, AggregationResult, InformativeResult
├── suspiciousactivityengine — SuspiciousActivityEngine, SuspiciousActivityEngineResult
└── validator            — DataValidator, SchemaValidator, TransactionValidator,
                           PersonalCustomerValidator, BusinessCustomerValidator
                           report/ValidationReport, report/ValidationIssue
```

---

## Validation

Before any analysis, DataFrames should be validated using one of the available validators.

`SchemaValidator` runs structural checks against any `DataFrameSchema`:

```java
SchemaValidator validator = new SchemaValidator(new TransactionSchema());
ValidationReport report = validator.validate(transactionDf);

if (report.hasErrors()) {
    report.getAllIssues().forEach(System.out::println);
}
```

Four checks are run:
1. **Column presence** — all required columns exist
2. **Column types** — values match the expected Java type
3. **Null values** — nulls are flagged as ERROR (if disallowed) or WARNING (if allowed)
4. **Allowed values** — constrained columns (e.g. Cash Turnover bands) only contain permitted values

For domain-specific validation, use the dedicated validators. Each one runs the full schema check first, then adds its own checks on top:

| Validator | Additional checks |
|---|---|
| `TransactionValidator` | Date/time validity, account number format, sort code format, ISO country codes |
| `PersonalCustomerValidator` | UK postcode format, date of birth/age range, ISO country codes |
| `BusinessCustomerValidator` | Date of incorporation, people list, cash turnover vs. turnover consistency, ISO country codes |

```java
TransactionValidator validator = new TransactionValidator();
ValidationReport report = validator.validate(transactionDf);
```

### ValidationReport

| Method | Description |
|---|---|
| `hasErrors()` | Returns true if any ERROR-severity issues were found |
| `hasWarnings()` | Returns true if any WARNING-severity issues were found |
| `getAllIssues()` | Returns all issues regardless of severity |
| `getIssues(Severity)` | Returns issues at a specific severity level |
| `getIssuesByColumn(String)` | Returns all issues for a named column |

### ValidationIssue severities

| Severity | Meaning |
|---|---|
| `ERROR` | Data is definitively wrong — missing column, wrong type |
| `WARNING` | Something suspicious that may be a data quality problem |
| `INFO` | Informational observation, not necessarily a problem |

---

## TransactionMap

`TransactionMap` runs a set of analysis modules over a customer's full transaction history. Each module receives the full `DataFrame` and returns any value. Results are collected into a `HashMap<String, Object>` keyed by module name.

### Module protocol

Modules may return anything, but SHOULD return one of two protocol types:

| Type | Use when |
|---|---|
| `AggregationResult` | The output is a derived/summarised `DataFrame` |
| `InformativeResult` | The output is a JSON string of derived facts |

### Built-in modules

| Module | Return type | Description |
|---|---|---|
| `CountryFrequencyModule` | `Map<String, Integer>` | Transaction count per country of origin |
| `LocationAverageModule` | `AggregationResult` | Country distribution table with counts and percentages, sorted by frequency |
| `AmountAggregationModule` | `AggregationResult` | Total, average, min and max transaction amounts (requires an `amount` column) |
| `TopBeneficiariesModule` | `InformativeResult` | JSON array of the top 5 beneficiaries by transaction count |

### Writing a custom module

Implement `TransactionMapModule<T>`:

```java
public class MyModule implements TransactionMapModule<InformativeResult> {

    @Override
    public String getModuleName() { return "MyModule"; }

    @Override
    public InformativeResult run(DataFrame df) {
        // read df, compute something, return result
    }
}
```

---

## SuspiciousActivityEngine

`SuspiciousActivityEngine` takes a single incoming transaction, the customer's profile, and the `HashMap` output from `TransactionMap`, then runs a set of scoring modules against them. Results are collected into a `SuspiciousActivityEngineResult` which can be serialised to JSON.

### Module output format

Every `SuspiciousActivityEngineModule` must return a `HashMap<String, Object>` with exactly these fields:

| Field | Type | Description |
|---|---|---|
| `Module Name` | `String` | Name of the module |
| `Module Ran` | `Boolean` | `true` if the module completed successfully |
| `Risk Score` | `Integer` | 0–100 risk score (higher = more expected behaviour / lower risk) |
| `Comments` | `String` | Human-readable explanation of the score |

If a module throws an uncaught exception, the engine catches it and records a fallback entry with `Module Ran: false` and `Risk Score: -1`.

### Built-in modules

| Module | Description |
|---|---|
| `ClassificationModule` | Scores the transaction's beneficiary country against the customer's historical country frequency |

### Writing a custom module

Implement `SuspiciousActivityEngineModule`:

```java
public class MyModule implements SuspiciousActivityEngineModule {

    @Override
    public String getModuleName() { return "MyModule"; }

    @Override
    public HashMap<String, Object> run(DataFrame transaction,
                                       DataFrame customerProfile,
                                       Object transactionMapResult) {
        HashMap<String, Object> output = new HashMap<>();
        output.put("Module Name", getModuleName());
        output.put("Module Ran", true);
        output.put("Risk Score", 80);
        output.put("Comments", "Explanation here.");
        return output;
    }
}
```

---

## Use Case Example

A new payment has arrived for customer 1042. We want to validate the data, profile their transaction history, and score the new transaction for fraud risk.

```java
// 1. Validate the incoming data
TransactionValidator txValidator = new TransactionValidator();
ValidationReport txReport = txValidator.validate(transactionHistoryDf);
if (txReport.hasErrors()) {
    throw new RuntimeException("Transaction data failed validation.");
}

PersonalCustomerValidator profileValidator = new PersonalCustomerValidator();
ValidationReport profileReport = profileValidator.validate(customerProfileDf);
if (profileReport.hasErrors()) {
    throw new RuntimeException("Customer profile failed validation.");
}

// 2. Build a profile of the customer's transaction history
HashMap<String, Object> transactionMap = new TransactionMap(transactionHistoryDf)
        .addModule(new CountryFrequencyModule())
        .addModule(new LocationAverageModule())
        .addModule(new TopBeneficiariesModule())
        .run();

// 3. Score the new incoming transaction against that profile
SuspiciousActivityEngineResult result = new SuspiciousActivityEngine(newTransactionDf, customerProfileDf, transactionMap)
        .addModule(new ClassificationModule())
        .run();

// 4. Output the risk assessment as JSON
System.out.println(result.toJson());
```

Example output:
```json
[
  {
    "Module Name" : "Classification",
    "Module Ran" : true,
    "Risk Score" : 20,
    "Comments" : "Beneficiary country 'NG' seen in 1 of 48 past transactions (score: 20/100)."
  }
]
```