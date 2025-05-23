package org.com.pangolin.domain.core.validacoes;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;


public final class Validacoes {
    private Validacoes() {}

    /**
     * Validador que verifica se o valor é não nulo
     * @param <T> Tipo do valor
     * @return Validator configurado
     */
    public static final Validator<Object> NAO_NULO =
            Validator.of(Objects::nonNull, "O valor não pode ser nulo");

    /*** Validador que verifica se a string não é vazia
     * @return Validator configurado para String
     */
    public static final Validator<String> NAO_VAZIO =
            Validator.of(s -> !s.trim().isEmpty(), "A string não pode ser vazia");
     /** Validador que verifica se o valor é não nulo e não vazio
     * @return Validator configurado para String
     */
    public static final Validator<String> NAO_NULO_NEM_VAZIO =
            NAO_NULO.evolveTo(String.class).and(NAO_VAZIO);

    /** Validador que verifica se o valor é positivo (maior ou igual a zero)
     * @param <Number> Tipo numérico
     * @return Validator configurado
     */
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
    /**
     * Evolui um validador para um tipo mais específico
     * @param validator Validador original
     * @param targetType Classe do novo tipo (para segurança)
     * @param <T> Tipo original do validador
     * @param <R> Novo tipo (deve ser subtipo de T)
     * @return Novo validador com tipo mais específico
     */
    public static <T, R extends T> Validator<R> evolveValidator(
            Validator<T> validator, Class<R> targetType) {
        return validator::validar;
    }

    /** Validador que verifica se o valor é não nulo e não vazio
     * @return Validator configurado para String
     */
    public static Validator<String> stringNaoNulaNemVazia() {
        return evolveValidator(NAO_NULO, String.class).and(NAO_VAZIO);
    }

    /**
     * Validador que verifica se o valor é maior que o limite especificado
     * @param limite Limite para comparação
     * @param <T> Tipo do valor (deve implementar Comparable)
     * @return Validator configurado
     */
    public static <T extends Comparable<T>> Validator<T> maiorQue(T limite) {
        return Validator.of(
                v -> v.compareTo(limite) > 0,
                "O valor deve ser maior que " + limite
        );
    }
    /** Validador que verifica se o valor é menor que o limite especificado
     * @param limite Limite para comparação
     * @param <T> Tipo do valor (deve implementar Comparable)
     * @return Validator configurado
     */
    public static Validator<String> carteiraId(String codido) {
        return  Validator.<String>of(Objects::nonNull, codido, "ID não pode ser nulo", true)
                .and(notBlankValidator())
                .and(startsWithPrefixValidator())
                .and(minLengthValidator(10))
                .and(notLettersOnlyValidator())
                .and(notLettersOnlyValidatorRegex());
    }

    // Validadores componentes
    private static Validator<String> notBlankValidator() {
        return Validator.of(s -> !s.isBlank(), " ID  não pode ser em branco");
    }

    private static Validator<String> startsWithPrefixValidator() {
        return Validator.of(s -> s.startsWith("WALLET-"), "ID não poder começar com Carteira - prefixo");
    }

    private static Validator<String> minLengthValidator(int minLength) {
        return Validator.of(s -> s.length() >= minLength,
                "O Id da Carteira deve ter um comprimento minimo  " + minLength + "caracteres");
    }
    private static Validator<String> notLettersOnlyValidator() {
        return Validator.of(
                s -> s.chars().anyMatch(Character::isDigit),
                "O Id da Carteira deve conter pelo menos um caractere numérico"
        );
    }
    private static Validator<String> notLettersOnlyValidatorRegex() {
        return Validator.of(
                s -> s.matches(".*[0-9].*"),
                "O Id da Carteira deve conter pelo menos um caractere numérico"
        );
    }



}


