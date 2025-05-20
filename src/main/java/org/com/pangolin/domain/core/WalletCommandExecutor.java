package org.com.pangolin.domain.core;

import java.util.function.Function;

/**
 * Executes and validates wallet-specific commands (e.g., transfers, deposits).
 * @param <Response> The result of the command (e.g., transaction receipt).
 * @param <Command> The input command (e.g., TransferCommand).
 */
public abstract class WalletCommandExecutor<Response, Command> {
    protected ValidationResult validationResult;

    public abstract void validateBusinessRules(Command command);

    public abstract Response execute(Command command);

    public static <Response, Command> Either<ValidationResult, Response> run(
            Command command,
            Function<Command, ValidationResult> validator,
            Function<Command, Response> executor) {
        ValidationResult result = validator.apply(command);
        return result.isValid()
                ? Either.right(executor.apply(command))
                : Either.left(result);
    }
}