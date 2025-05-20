package org.com.pangolin.domain.core;

import java.util.function.Function;


public abstract class ValidateModel<I> {
    protected ValidationResult validationResult;


    public abstract void validate(I model);


    public static <I> Either<ValidationResult, I> create(
            I model,
            Function<I, ValidationResult> validator) {
        ValidationResult result = validator.apply(model);
        if (result.isValid()) {
            return Either.right(model);
        } else {
            return Either.left(result);
        }
    }
}
