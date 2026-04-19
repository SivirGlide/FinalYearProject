# Fraud Detection Library

A modular, Java-based fraud detection library built on top of a dataframe processing layer (`DFLib`) and a pluggable suspicious activity scoring engine.

---

## Overview

This library evaluates incoming transactions for potential fraud by combining:

- historical transaction data  
- customer (personal) information  
- business data  
- configurable fraud detection modules  
- a scoring engine for new transactions  

It is designed to be **extensible**, allowing users to:

- define custom fraud detection modules  
- introduce custom schemas and data structures  
- tailor fraud logic to domain-specific patterns  

At its core, the system uses `DFLib` to:

- load and structure datasets  
- validate and clean data  
- transform and aggregate features  
- prepare inputs for fraud scoring  

---

## Architecture

The system operates in two main stages:

1. **Data Preparation (Validation + Transformation)**
2. **Fraud Scoring (Risk Evaluation)**
---

---

## Core Concepts

### Modular Design

Fraud detection logic is implemented through **modules**.

Modules can be:

- built-in (e.g. thresholds, velocity checks, anomaly detection)
- user-defined (custom fraud rules)

Modules are injected into:

- **TransactionMap** → feature enrichment  
- **Suspicious Activity Engine** → scoring logic  

This allows fraud behaviour to evolve without modifying the core system.

---

## Schemas

The library provides built-in schemas for:

- **Transaction data**
- **Customer (personal) data**
- **Business data**

These define the default structure expected by the system.

### Custom Schemas

Users can define their own schemas to support:

- additional domain-specific fields  
- alternative data models  
- enriched datasets (e.g. device info, geo data, behavioural signals)  

Custom schemas integrate via `DFLib` and can:

- participate in validation pipelines  
- be consumed by modules  
- contribute to feature engineering and scoring  

This enables the system to adapt to different industries and use cases without core changes.

---

## Application Flows

### 1. Data Preparation Flow (`sourceValidation`)

Ensures all data is clean, valid, and ready for processing.

#### Flow

1. Source provides raw input data  
2. Data is loaded into `DFLib`  
3. Data is validated  
4. Data is modified (if required)  
5. Validation and modification repeat until clean  

---

### 2. Fraud Scoring Flow (`riskManagementCalculation`)

Evaluates new transactions using historical and contextual data.

#### Flow

1. `DFLib (Transactions)` provides historical data  
2. Modules are injected into an empty `TransactionMap`  
3. `TransactionMap` structures and enriches data  
4. Aggregated customer profile is generated  
5. `DFLib (Customer + Business data)` is provided  
6. A new transaction is submitted  
7. Modules are injected into the scoring engine  
8. Fraud score is produced  

---

## Key Components

### `DFLib`
Responsible for:

- data ingestion  
- validation  
- transformation  
- aggregation  

---

### `TransactionMap`

Combines:

- transaction datasets  
- module-generated features  

Used to create a structured representation of transaction behaviour.

---

### `Aggregated Customer Profile`

A derived view of customer activity used to:

- provide behavioural context  
- improve fraud detection accuracy  

---

### `Suspicious Activity Engine`

Core engine responsible for:

- evaluating transactions  
- applying fraud logic  
- generating a fraud score  

---

### `Modules`

Pluggable units that:

- enrich features  
- apply fraud rules  
- influence scoring  

---

## Usage

### Basic Example

```java
// Load data
DFLib transactions = loadTransactions();
DFLib customers = loadCustomers();

// Validate & transform
transactions = validate(transactions);

// Build transaction map
TransactionMap map = new TransactionMap();
map.injectModules(transactionModules);
map.load(transactions);

// Aggregate customer profile
CustomerProfile profile = map.aggregate();

// Initialise engine
SuspiciousActivityEngine engine = new SuspiciousActivityEngine();
engine.injectModules(scoringModules);

// Evaluate transaction
FraudScore score = engine.evaluate(newTransaction, profile, customers);