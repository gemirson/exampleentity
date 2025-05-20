package org.com.pangolin.domain;

import org.com.pangolin.domain.core.Either;
import org.com.pangolin.domain.core.ValidateModel;
import org.com.pangolin.domain.core.ValidationResult;


/**
        * Validates {@link TransferCommand} objects according to business rules.
 * Extends {@link ValidateModel} to provide transfer-specific validation logic.
 *
         * <p>Usage example:
        * <pre>{@code
 * TransferCommand cmd = new TransferCommand(...);
 * Either<ValidationResult, TransferCommand> result = ValidateTransferCommand.createAndValidate(cmd);
 *
 * if (result.isRight()) {
 *     // Proceed with valid transfer
 * } else {
 *     // Handle validation errors
 * }
 * }</pre>
        */
public class ValidateTransferCommand  extends ValidateModel<TransferCommand> {
    /**
     * Validates the transfer command according to business rules.
     * Sets the validation result which can be retrieved via {@link #getValidationResult()}.
     *
     * @param model the transfer command to validate (must not be null)
     * @throws NullPointerException if model is null
     */
    @Override
    public void validate(TransferCommand model) {
        this.validationResult = ValidationResult.invalid("AMOUNT",
                new ValidationResult.ValidationError.Builder("INVALID_AMOUNT")
                        .withMessageKey("AMOUNT cannot be empty or null")
                        .build());
    }
    /**
     * Factory method that creates and validates a transfer command in one operation.
     *
     * @param command the transfer command to validate
     * @return {@link Either} containing either a {@link ValidationResult} (Left)
     *         or the validated {@link TransferCommand} (Right)
     */
    public static Either<ValidationResult, TransferCommand> createAndValidate(TransferCommand command) {
        return ValidateModel.create(
                command ,
                ValidateTransferCommand.create()::validateAndGetResult
        );
    }

    /**
     * Internal method that executes validation and returns the result.
     */
    private ValidationResult validateAndGetResult(TransferCommand command) {
        validate(command);
        return this.validationResult;
    }
    /**
     * Creates a new instance of the validator.
     *
     * @return new {@link ValidateTransferCommand} instance
     */
    public  static  ValidateTransferCommand  create(){
        return  new ValidateTransferCommand ();

    }
}
