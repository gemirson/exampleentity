package org.com.pangolin.domain.core;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;


public final class Validacoes {
    private Validacoes() {}

    public static final Validator<Object> NAO_NULO =
            Validator.of(Objects::nonNull, "O valor não pode ser nulo");

    public static final Validator<String> NAO_VAZIO =
            Validator.of(s -> !s.isEmpty(), "A string não pode ser vazia");

    // Validador combinado pronto para uso
    public static final Validator<String> NAO_NULO_NEM_VAZIO =
            NAO_NULO.evolveTo(String.class).and(NAO_VAZIO);
    public static final Validator<Number> POSITIVO =
            Validator.of(n -> n.doubleValue() >= 0, "O valor deve ser positivo");

    public static final Validator<BigDecimal> MAIOR_QUE_ZERO =
            Validator.of(v -> v.compareTo(BigDecimal.ZERO) > 0, "O valor deve ser maior que zero");

    // Correção: usando método genérico estático
    public static <T> Validator<List<T>> listaNaoVazia() {
        return Validator.of(list -> !list.isEmpty(), "A lista não pode ser vazia");
    }

    /**
     * Validador que verifica se um elemento NÃO está presente na lista fornecida
     * @param <T> Tipo do elemento
     * @param coleção Lista onde verificar a ausência do elemento
     * @param mensagemErro Mensagem de erro personalizada (opcional)
     * @return Validator configurado
     */
    public static <T> Validator<T> naoContidoEm(Collection<? extends T> colecao, String mensagemErro) {
        Objects.requireNonNull(colecao, "Coleção não pode ser nula");
        String mensagem = mensagemErro != null ? mensagemErro :
                "O elemento não pode estar presente na coleção fornecida";

        return Validator.of(
                elemento -> !colecao.contains(elemento),
                mensagem
        );
    }

    // Versão com mensagem padrão
    public static <T> Validator<T> naoContidoEm(Collection<? extends T> colecao) {
        return naoContidoEm(colecao, null);
    }
    /**
     * Validador que verifica se nenhum elemento da coleção atende ao predicado
     * @param coleção Coleção a verificar
     * @param predicado Condição para verificar
     * @param mensagemErro Mensagem de erro
     * @return Validator configurado
     */
    public static <T> Validator<T> nenhumAtende(
            Collection<? extends T> colecao,
            Predicate<? super T> predicado,
            String mensagemErro) {

        Objects.requireNonNull(colecao, "Coleção não pode ser nula");
        Objects.requireNonNull(predicado, "Predicado não pode ser nulo");

        return Validator.of(
                elemento -> colecao.stream().noneMatch(predicado),
                mensagemErro
        );
    }
    // Versão segura para evolução de validador
    public static <T, R extends T> Validator<R> evolveValidator(
            Validator<T> validator, Class<R> targetType) {
        return validator::validar;
    }

    // Método para strings não nulas e não vazias
    public static Validator<String> stringNaoNulaNemVazia() {
        return evolveValidator(NAO_NULO, String.class).and(NAO_VAZIO);
    }

    public static <T extends Comparable<T>> Validator<T> maiorQue(T limite) {
        return Validator.of(
                v -> v.compareTo(limite) > 0,
                "O valor deve ser maior que " + limite
        );
    }



}


