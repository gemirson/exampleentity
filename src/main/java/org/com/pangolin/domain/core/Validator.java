package org.com.pangolin.domain.core;

import java.util.Objects;
import java.util.function.Predicate;

@FunctionalInterface
public interface Validator<T>  {
    void validar(T value) throws ValidacaoException;

    default <U extends T> Validator<U> and(Validator<? super T> other) {
        Objects.requireNonNull(other, "Outro validador não pode ser nulo");
        return value -> {
            this.validar(value);
            other.validar(value);
        };
    }

    static <T> Validator<T> of(Predicate<T> predicate, String mensagemErro) {
        Objects.requireNonNull(predicate, "Predicado não pode ser nulo");
        Objects.requireNonNull(mensagemErro, "Mensagem de erro não pode ser nula");

        return value -> {
            if (!predicate.test(value)) {
                throw new ValidacaoException(mensagemErro);
            }
        };
    }

    // Versão para converter Validator de supertipo
    static <T> Validator<T> from(Validator<? super T> validator) {
        return validator::validar;
    }

    /**
     * Converte o validador para um tipo mais específico
     * @param <R> Novo tipo (deve ser subtipo de T)
     * @param targetType Classe do novo tipo (para segurança)
     * @return Novo validador com tipo mais específico
     */
    default <R extends T> Validator<R> evolveTo(Class<R> targetType) {
        Objects.requireNonNull(targetType, "Target type cannot be null");
        return this::validar;
    }

    /**
     * Versão sem parâmetro Class para uso fluente
     */
    default <R extends T> Validator<R> evolveTo() {
        return this::validar;
    }
    class ValidacaoException extends RuntimeException {
        public ValidacaoException(String message) {
            super(message);
        }
    }
}
