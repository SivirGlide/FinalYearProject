# Fraud Detection Library

A Java-based fraud detection dependency built around a dataframe processing library (`DFLib`) and a modular suspicious activity scoring engine.

---

## Overview

This library evaluates incoming transactions for potential fraud by combining:

- historical transaction data  
- customer information  
- configurable fraud detection modules  
- a scoring engine for new transactions  

It also allows users to define custom modules, enabling tailored fraud detection strategies based on specific business rules or pattern recognition needs.

At its core, the system uses `DFLib` for loading, validating, transforming, and aggregating data before passing it into the fraud detection engine.

---

## Core Concepts

### Modular Design

Fraud detection logic is implemented through **modules**. These can be:

- built-in modules (e.g. thresholds, velocity checks, pattern detection)
- custom user-defined modules

Modules can be injected into:

- the **TransactionMap** (for feature enrichment)
- the **Suspicious Activity Engine** (for scoring logic)

This makes the system highly extensible without requiring changes to the core pipeline.

### Schema Flexibility

The library provides built-in schemas for:

- **Transaction data**
- **Customer (personal) data**
- **Business data**

These schemas define the default structure expected by the system for processing and scoring.

In addition, users can define and supply their own custom schemas to support:

- additional fields specific to their domain  
- alternative data structures  
- enriched datasets (e.g. device data, location metadata, behavioural signals)  

Custom schemas can be integrated into the pipeline through `DFLib`, allowing them to:

- participate in validation and transformation  
- be consumed by modules  
- contribute to transaction enrichment and scoring  

This ensures the system can adapt to different data models without requiring changes to the core engine.

## Architecture

The system is composed of two primary stages:

1. **Data preparation (validation and transformation)**
2. **Fraud scoring (risk evaluation)**

Data flows from raw sources through `DFLib`, into a structured transaction context, and finally into the scoring engine.


## Application Flows

### 1. Data Preparation Flow (`sourceValidation`)

Raw source data is processed through a validation and transformation loop to ensure consistency and quality.

#### Flow

1. **Source** provides raw input data  
2. Data is loaded into **DFLib**  
3. Data is **validated**  
4. If necessary, data is **modified**  
5. Steps 3–4 repeat until the dataset is clean and ready  

This ensures the fraud engine operates on normalized, reliable data.

---

### 2. Fraud Scoring Flow (`riskManagementCalculation`)

Once data is prepared, the system builds a transaction context and evaluates new transactions.

#### Flow

1. **DFLib (Transactions)** provides historical transaction data  
2. **Modules** are injected into an empty **TransactionMap**  
3. The **TransactionMap** enriches and structures transaction data  
4. An **Aggregated Customer Profile** is generated  
5. **DFLib (Customer Information)** is supplied to the engine  
6. A **New Transaction** is submitted for evaluation  
7. Additional **modules** are injected into the **Suspicious Activity Engine**  
8. The engine produces a **fraud score**  

---

## Key Components

### `DFLib`
Handles dataframe operations including:

- ingestion  
- transformation  
- validation  
- aggregation  

---

### `TransactionMap`
Combines:

- transaction data  
- module-generated features  

Used to build a structured representation of transaction behavior.

---

### `Aggregated Customer Profile`
Derived from transaction data and used to provide contextual insights during fraud evaluation.

---

### `Suspicious Activity Engine`
Core engine responsible for:

- evaluating new transactions  
- applying module logic  
- generating the final fraud score  

---

### `Modules`
Pluggable units of logic used for:

- feature generation  
- rule evaluation  
- scoring adjustments  

---

## Usage

### Basic Flow

```java
// 1. Load and prepare data
DFLib transactions = loadTransactions();
DFLib customers = loadCustomers();

// 2. Validate and transform
transactions = validateAndTransform(transactions);

// 3. Build TransactionMap
TransactionMap map = new TransactionMap();
map.injectModules(modules);
map.load(transactions);

// 4. Aggregate customer data
CustomerProfile profile = map.aggregate();

// 5. Initialise engine
SuspiciousActivityEngine engine = new SuspiciousActivityEngine();
engine.injectModules(scoringModules);

// 6. Score new transaction
FraudScore score = engine.evaluate(newTransaction, profile, customers);