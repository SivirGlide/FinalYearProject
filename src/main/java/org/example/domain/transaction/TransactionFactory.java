package org.example.domain.transaction;

import java.util.UUID;

public class TransactionFactory {

    /**
     * Creates a standard outward payment — customer sending money out.
     */
    public static Transaction createOutwardTransaction(
            int customerId,
            double amount,
            String currency,
            String senderAccountNumber,
            String senderSortCode,
            String receiverAccountNumber,
            String receiverSortCode,
            String originCountry,
            String destinationCountry,
            String reference
    ) {
        validate(amount, currency, originCountry, destinationCountry);

        return new Transaction(
                generateId(),
                customerId,
                amount,
                currency,
                Transaction.TransactionType.OUTWARD,
                Transaction.TransactionStatus.PENDING,
                senderAccountNumber,
                senderSortCode,
                receiverAccountNumber,
                receiverSortCode,
                originCountry,
                destinationCountry,
                reference
        );
    }

    /**
     * Creates a standard inward payment — customer receiving money.
     */
    public static Transaction createInwardTransaction(
            int customerId,
            double amount,
            String currency,
            String senderAccountNumber,
            String senderSortCode,
            String receiverAccountNumber,
            String receiverSortCode,
            String originCountry,
            String destinationCountry,
            String reference
    ) {
        validate(amount, currency, originCountry, destinationCountry);

        return new Transaction(
                generateId(),
                customerId,
                amount,
                currency,
                Transaction.TransactionType.INWARD,
                Transaction.TransactionStatus.PENDING,
                senderAccountNumber,
                senderSortCode,
                receiverAccountNumber,
                receiverSortCode,
                originCountry,
                destinationCountry,
                reference
        );
    }

    /**
     * Creates an internal transfer between two accounts within the same system.
     * Origin and destination country are the same — no cross-border movement.
     */
    public static Transaction createInternalTransaction(
            int customerId,
            double amount,
            String currency,
            String senderAccountNumber,
            String receiverAccountNumber,
            String country,
            String reference
    ) {
        validate(amount, currency, country, country);

        return new Transaction(
                generateId(),
                customerId,
                amount,
                currency,
                Transaction.TransactionType.INTERNAL,
                Transaction.TransactionStatus.PENDING,
                senderAccountNumber,
                null,  // internal transfers don't use sort codes
                receiverAccountNumber,
                null,
                country,
                country,
                reference
        );
    }

    // ── Shared validation ──────────────────────────────────────────────

    private static void validate(double amount, String currency, String originCountry, String destinationCountry) {
        if (amount <= 0)
            throw new IllegalArgumentException("Transaction amount must be greater than zero.");

        if (currency == null || currency.isBlank() || currency.length() != 3)
            throw new IllegalArgumentException("Currency must be a 3-character ISO 4217 code e.g. 'GBP'.");

        if (originCountry == null || originCountry.isBlank() || originCountry.length() != 2)
            throw new IllegalArgumentException("Origin country must be a 2-character ISO code e.g. 'GB'.");

        if (destinationCountry == null || destinationCountry.isBlank() || destinationCountry.length() != 2)
            throw new IllegalArgumentException("Destination country must be a 2-character ISO code e.g. 'US'.");
    }

    /** Generates a unique transaction ID using UUID */
    private static String generateId() {
        return "TXN-" + UUID.randomUUID().toString().toUpperCase();
    }
}
