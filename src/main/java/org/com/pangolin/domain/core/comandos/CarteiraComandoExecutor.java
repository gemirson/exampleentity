package org.com.pangolin.domain.core.comandos;

import org.com.pangolin.domain.core.Either;
import org.com.pangolin.domain.core.ValidationResult;

import java.util.function.Function;

public abstract class CarteiraComandoExecutor <Resposta, Entrada>  {

    public abstract void validarBusinessRules(Entrada command);

    public abstract Resposta execute(Entrada command);

    public static <Resposta, Entrada> Either<ValidationResult, Resposta> aplicar(
            Entrada command,
            Function<Entrada, ValidationResult> validator,
            Function<Entrada, Resposta> executor) {
        ValidationResult result = validator.apply(command);
        return result.isValid()
                ? Either.right(executor.apply(command))
                : Either.left(result);
    }
}
