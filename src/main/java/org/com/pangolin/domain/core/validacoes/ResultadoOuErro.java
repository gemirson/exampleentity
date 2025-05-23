package org.com.pangolin.domain.core.validacoes;

import java.util.Objects;
import java.util.function.Function;

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
