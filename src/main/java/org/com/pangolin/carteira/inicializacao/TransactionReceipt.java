package org.com.pangolin.carteira.inicializacao;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a receipt confirming the execution of a wallet transaction.
 * Immutable record containing all relevant transaction details.
 *
 * @param transactionId Unique identifier for the transaction
 * @param timestamp When the transaction was processed
 * @param sourceWalletId Origin wallet identifier
 * @param targetWalletId Destination wallet identifier (nullable for deposits/withdrawals)
 * @param amount Transaction amount
 * @param transactionType Type of transaction (TRANSFER, DEPOSIT, WITHDRAWAL)
 * @param status Current status of the transaction (SUCCESS, FAILED, PENDING)
 * @param fee Applied transaction fee (if any)
 */
public record TransactionReceipt(
        UUID transactionId,
        LocalDateTime timestamp,
        String sourceWalletId,
        String targetWalletId,
        double amount,
        TransactionType transactionType,
        TransactionStatus status,
        double fee
) {
    /**
     * Types of supported wallet transactions.
     */
    public enum TransactionType {
        TRANSFER,
        DEPOSIT,
        WITHDRAWAL
    }

    /**
     * Possible transaction states.
     */
    public enum TransactionStatus {
        SUCCESS,
        FAILED,
        PENDING,
        REVERSED
    }

    /**
     * Creates a new receipt builder for fluent construction.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder pattern for TransactionReceipt to handle optional fields.
     */
    public static class Builder {
        private UUID transactionId;
        private LocalDateTime timestamp;
        private String sourceWalletId;
        private String targetWalletId;
        private double amount;
        private TransactionType transactionType;
        private TransactionStatus status;
        private double fee;

        public Builder() {
            this.transactionId = UUID.randomUUID();
            this.timestamp = LocalDateTime.now();
            this.status = TransactionStatus.SUCCESS;
            this.fee = 0.0;
        }

        public Builder sourceWalletId(String sourceWalletId) {
            this.sourceWalletId = sourceWalletId;
            return this;
        }

        public Builder targetWalletId(String targetWalletId) {
            this.targetWalletId = targetWalletId;
            return this;
        }

        public Builder amount(double amount) {
            this.amount = amount;
            return this;
        }

        public Builder transactionType(TransactionType type) {
            this.transactionType = type;
            return this;
        }

        public Builder status(TransactionStatus status) {
            this.status = status;
            return this;
        }

        public Builder fee(double fee) {
            this.fee = fee;
            return this;
        }

        public TransactionReceipt build() {
            return new TransactionReceipt(
                    transactionId,
                    timestamp,
                    sourceWalletId,
                    targetWalletId,
                    amount,
                    transactionType,
                    status,
                    fee
            );
        }
    }

    /**
     * Formats the receipt as a human-readable string.
     */
    public String toDisplayString() {
        return String.format(
                """
                Transaction ID: %s
                Type: %s
                Status: %s
                Amount: %.2f
                Fee: %.2f
                From: %s
                To: %s
                Timestamp: %s
                """,
                transactionId,
                transactionType,
                status,
                amount,
                fee,
                sourceWalletId,
                targetWalletId != null ? targetWalletId : "N/A",
                timestamp
        );
    }
}
