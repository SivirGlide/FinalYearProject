package org.example.domain.transaction;

import java.time.LocalDateTime;

public class Transaction {

    // ── Core identifiers ──────────────────────────────────────────────
    private final String transactionId;       // unique ID for this transaction
    private final int customerId;             // links back to the Customer

    // ── Money ─────────────────────────────────────────────────────────
    private double amount;
    private String currency;                  // ISO 4217 e.g. "GBP", "USD", "EUR"

    // ── Type & Status ─────────────────────────────────────────────────
    private TransactionType type;
    private TransactionStatus status;

    // ── Parties ───────────────────────────────────────────────────────
    private String senderAccountNumber;
    private String senderSortCode;
    private String receiverAccountNumber;
    private String receiverSortCode;

    // ── Geography ─────────────────────────────────────────────────────
    private String originCountry;             // ISO 2-char e.g. "GB"
    private String destinationCountry;        // ISO 2-char e.g. "US"

    // ── Meta ──────────────────────────────────────────────────────────
    private String reference;                 // payment reference/description
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor — only called by the factory
    Transaction(
            String transactionId,
            int customerId,
            double amount,
            String currency,
            TransactionType type,
            TransactionStatus status,
            String senderAccountNumber,
            String senderSortCode,
            String receiverAccountNumber,
            String receiverSortCode,
            String originCountry,
            String destinationCountry,
            String reference
    ) {
        this.transactionId       = transactionId;
        this.customerId          = customerId;
        this.amount              = amount;
        this.currency            = currency;
        this.type                = type;
        this.status              = status;
        this.senderAccountNumber = senderAccountNumber;
        this.senderSortCode      = senderSortCode;
        this.receiverAccountNumber = receiverAccountNumber;
        this.receiverSortCode    = receiverSortCode;
        this.originCountry       = originCountry;
        this.destinationCountry  = destinationCountry;
        this.reference           = reference;
        this.createdAt           = LocalDateTime.now();
        this.updatedAt           = LocalDateTime.now();
    }

    // ── Enums ─────────────────────────────────────────────────────────

    public enum TransactionType {
        INWARD,     // money coming in
        OUTWARD,    // money going out
        INTERNAL    // between accounts within the same system
    }

    public enum TransactionStatus {
        PENDING,
        COMPLETED,
        FAILED,
        FLAGGED    // suspicious — needs review
    }

    // ── Getters ───────────────────────────────────────────────────────

    public String getTransactionId()        { return transactionId; }
    public int getCustomerId()              { return customerId; }
    public double getAmount()               { return amount; }
    public String getCurrency()             { return currency; }
    public TransactionType getType()        { return type; }
    public TransactionStatus getStatus()    { return status; }
    public String getSenderAccountNumber()  { return senderAccountNumber; }
    public String getSenderSortCode()       { return senderSortCode; }
    public String getReceiverAccountNumber(){ return receiverAccountNumber; }
    public String getReceiverSortCode()     { return receiverSortCode; }
    public String getOriginCountry()        { return originCountry; }
    public String getDestinationCountry()   { return destinationCountry; }
    public String getReference()            { return reference; }
    public LocalDateTime getCreatedAt()     { return createdAt; }
    public LocalDateTime getUpdatedAt()     { return updatedAt; }

    // ── Setters — only mutable fields (id and timestamps are final) ───

    public void setAmount(double amount)                  { this.amount = amount; updateTimestamp(); }
    public void setCurrency(String currency)              { this.currency = currency; updateTimestamp(); }
    public void setStatus(TransactionStatus status)       { this.status = status; updateTimestamp(); }
    public void setSenderAccountNumber(String number)     { this.senderAccountNumber = number; updateTimestamp(); }
    public void setSenderSortCode(String sortCode)        { this.senderSortCode = sortCode; updateTimestamp(); }
    public void setReceiverAccountNumber(String number)   { this.receiverAccountNumber = number; updateTimestamp(); }
    public void setReceiverSortCode(String sortCode)      { this.receiverSortCode = sortCode; updateTimestamp(); }
    public void setOriginCountry(String country)          { this.originCountry = country; updateTimestamp(); }
    public void setDestinationCountry(String country)     { this.destinationCountry = country; updateTimestamp(); }
    public void setReference(String reference)            { this.reference = reference; updateTimestamp(); }
    public void setType(TransactionType type)             { this.type = type; updateTimestamp(); }

    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + transactionId + '\'' +
                ", customerId=" + customerId +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", from=" + originCountry +
                ", to=" + destinationCountry +
                ", createdAt=" + createdAt +
                '}';
    }
}