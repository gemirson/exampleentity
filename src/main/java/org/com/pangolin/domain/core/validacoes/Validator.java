package org.com.pangolin.domain.core.validacoes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

@FunctionalInterface
public interface Validator<T>  {

    public  static final String CODE_PADRAO = "99999";
    /**
     * Interface para validação de objetos genéricos
     * @param <T> Tipo do objeto a ser validado
     */
    /**
     * Valida o objeto e retorna um ResultadoValidacao
     * @param value Objeto a ser validado
     * @return Resultado da validação
     * @throws ValidacaoException Se a validação falhar e o usuário preferir tratamento por exceção
     */
    List<ErrosValidacao> validar(T value) throws ValidacaoException;

    /**
     * Combina com outro validador (AND lógico)
     */
    default Validator<T> and(Validator<? super T> other) {
        return value -> {
            List<ErrosValidacao> erros1 = this.validar(value);
            List<ErrosValidacao> erros2 = other.validar(value);

            List<ErrosValidacao> todosErros = new ArrayList<>();
            todosErros.addAll(erros1);
            todosErros.addAll(erros2);

            // Verifica se deve lançar exceção
            boolean deveLancar = todosErros.stream()
                    .anyMatch(ErrosValidacao::deveLancarExcecao);

            if (deveLancar) {
                throw new ValidacaoException(String.valueOf(todosErros.stream()
                        .filter(ErrosValidacao::deveLancarExcecao)
                        .findFirst()
                        .map((Function<? super ErrosValidacao, ? extends String>) ErrosValidacao::toString)));
            }

            return todosErros;
        };
    }

    /**
     * Cria um validador básico
     */
    static <T> Validator<T> of(
            Predicate<T> predicate,
            String codigoErro,
            String mensagemErro,
            boolean lancarExcecao
    ) {
        return value -> {
            if (!predicate.test(value)) {
                return List.of((ErrosValidacao) ErrosValidacao.criar(

                        codigoErro,
                        mensagemErro,
                        lancarExcecao
                ));
            }
            return List.of();
        };
    }

    /**
     * Versão simplificada sem lançar exceção
     */
    static <T> Validator<T> of(
            Predicate<T> predicate,
                        String codigoErro,
            String mensagemErro
    ) {
        return of(predicate, codigoErro, mensagemErro, false);
    }

    /**
     * Versão simplificada sem lançar exceção
     */
    static <T> Validator<T> of(
            Predicate<T> predicate,
            String mensagemErro
    ) {
        return of(predicate,CODE_PADRAO, mensagemErro, false);
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
