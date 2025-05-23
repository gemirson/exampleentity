package org.com.pangolin.domain.core;

import java.util.Objects;

/**
 * Interface para evolução segura de tipos em validadores
 * @param <T> Tipo original do validador
 */
public interface ValidatorEvolution<T> extends Validator<T> {

    /**
     * Converte o validador para um tipo mais específico
     * @param <R> Novo tipo (deve ser subtipo de T)
     * @param targetType Classe do novo tipo (para segurança)
     * @return Novo validador com tipo mais específico
     */
    default <R extends T> Validator<R> evolveTo(Class<R> targetType) {
        Objects.requireNonNull(targetType, "Target type cannot be null");
        return value -> this.validar(value);
    }

    /**
     * Versão sem parâmetro Class para uso fluente
     */
    default <R extends T> Validator<R> evolveTo() {
        return value -> this.validar(value);
    }
}
