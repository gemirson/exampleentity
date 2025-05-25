package org.com.pangolin.domain.core.validacoes;

import org.com.pangolin.domain.Carteira;

import java.util.Objects;
import java.util.function.Function;

/**
 * A functional programming construct that represents a value of one of two possible types (a disjoint union).
 *
 * <p>An {@code Either} is either a {@code left} (typically representing a failure case) or
 * a {@code right} (typically representing a success case), but never both. This class is
 * commonly used for error handling where you want to explicitly represent both success and
 * failure cases in the type system.</p>
 *
 * <p><b>Typical Usage:</b></p>
 * <pre>{@code
 * Either<ValidationError, Wallet> result = Wallet.create(...);
 *
 * if (result.isRight()) {
 *     Wallet wallet = result.getRight();
 *     // handle success case
 * } else {
 *     ValidationError error = result.getLeft();
 *     // handle error case
 * }
 *
 * // Or using functional style:
 * String message = result.fold(
 *     error -> "Error: " + error.getMessage(),
 *     wallet -> "Created wallet with balance: " + wallet.getBalance()
 * );
 * }</pre>
 *
 * @param <L> the type of the left value (typically represents an error/failure)
 * @param <R> the type of the right value (typically represents a success result)
 */
public sealed interface ResultadoOuErro<L, R> {
    record Esquerdo<L, R>(L valor) implements ResultadoOuErro<L, R> {}
    record Direito<L, R>(R valor) implements ResultadoOuErro<L, R> {}

    default <T> T desdobrar(
            Function<L, T> funcaoEsquerda,
            Function<R, T> funcaoDireita
    ) {
        Objects.requireNonNull(funcaoEsquerda, "Função esquerda não pode ser nula");
        Objects.requireNonNull(funcaoDireita, "Função direita não pode ser nula");

        return switch (this) {
            case Esquerdo<L, R>(var valor) -> funcaoEsquerda.apply(valor);
            case Direito<L, R>(var valor) -> funcaoDireita.apply(valor);
        };
    }

    // Métodos auxiliares
    static <L, R> ResultadoOuErro<L, R> esquerdo(L valor) {
        return new Esquerdo<>(valor);
    }

    static <L, R> ResultadoOuErro<L, R> direito(R valor) {
        return new Direito<>(valor);
    }

    default boolean isEsquerdo() {
        return this instanceof Esquerdo<?, ?>;
    }
    default boolean isDireito() {
        return this instanceof Direito<?, ?>;
    }
}
