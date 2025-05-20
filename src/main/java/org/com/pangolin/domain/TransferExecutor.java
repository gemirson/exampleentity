package org.com.pangolin.domain;

import org.com.pangolin.domain.core.Either;
import org.com.pangolin.domain.core.TransactionReceipt;
import org.com.pangolin.domain.core.ValidationResult;
import org.com.pangolin.domain.core.WalletCommandExecutor;

import java.util.Objects;


/**
 * Executes transfer commands between wallets, handling validation and transaction processing.
 * Extends {@link WalletCommandExecutor} to provide specific transfer functionality.
 *
 * <p>Responsibility chain:
 * <ol>
 *   <li>Validates transfer business rules
 *   <li>Processes the transfer if valid
 *   <li>Generates a transaction receipt
 * </ol>
 *
 * <p>Example usage:
 * <pre>{@code
 * TransferCommand cmd = new TransferCommand(...);
 * Either<ValidationResult, TransactionReceipt> result = TransferExecutor.createAndProcess(cmd);
 *
 * if (result.isRight()) {
 *     TransactionReceipt receipt = result.get();
 *     // Process successful transfer
 * } else {
 *     ValidationResult errors = result.getLeft();
 *     // Handle validation failures
 * }
 * }</pre>
 */
public class TransferExecutor extends WalletCommandExecutor<TransactionReceipt, TransferCommand> {

    /**
     * Validates transfer command business rules.
     * @param command the transfer command to validate
     * @throws NullPointerException if command is null
     */
    @Override
    public void validateBusinessRules(TransferCommand command) {
        Objects.requireNonNull(command, "TransferCommand cannot be null");
        if (command.amount <= 0) {
            this.validationResult = ValidationResult.invalid("AMOUNT",
                    new ValidationResult.ValidationError.Builder("INVALID_AMOUNT")
                            .withMessageKey("AMOUNT cannot be empty or null")
                            .build());
        }

    }

    /**
     * Executes the transfer and generates a transaction receipt.
     * @param command the validated transfer command
     * @return transaction receipt with transfer details
     */
    @Override
    public TransactionReceipt execute(TransferCommand command) {
        Objects.requireNonNull(command, "TransferCommand cannot be null");
        return TransactionReceipt.builder()
                .sourceWalletId("wallet-123")
                .targetWalletId("wallet-456")
                .amount(100.50)
                .transactionType(TransactionReceipt.TransactionType.TRANSFER)
                .fee(1.50)
                .build();
    }


    /**
     * Creates and processes a transfer command in one operation.
     * @param command the transfer command to execute
     * @return Either a validation error (Left) or transaction receipt (Right)
     */
    public  static Either<ValidationResult, TransactionReceipt>  createAndProcess(TransferCommand command){
        Objects.requireNonNull(command, "TransferCommand cannot be null");
        TransferExecutor executor= TransferExecutor.Builder.builder();
            return  run(
                    command,
                    executor::validateAndGetResult,
                    executor::execute
            );
    }

    /**
     * Internal validation method that returns the validation result.
     */
    private ValidationResult validateAndGetResult(TransferCommand command) {
        validateBusinessRules(command);
        return this.validationResult;
    }

    /**
     * Creates a new TransferExecutor instance.
     * @return new TransferExecutor builder
     */
    public  static Builder builder(){
        return  new Builder();
    }

    /**
     * Builder for TransferExecutor (currently simple factory, can be enhanced for complex initialization).
     */

    public static  class  Builder{

        public Builder(){}

        /**
         * Creates a new TransferExecutor instance.
         * @return new TransferExecutor instance
         */
        public  static  TransferExecutor builder(){
            return  new TransferExecutor();
        }
    }
}
