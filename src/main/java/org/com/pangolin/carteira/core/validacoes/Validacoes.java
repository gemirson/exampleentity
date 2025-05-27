package org.com.pangolin.carteira.core.validacoes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
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

    public static <T extends Number & Comparable<T>> Validator<T> val_intervalo(T min, T max) {
        return Validator.of(
                n -> n.compareTo(min) >= 0 && n.compareTo(max) <= 0,
                String.format("Deve estar entre %s e %s", min, max)
        );
    }

    public static Validator<String> val_str_nao_vazia() {
        return Validator.of(
                s -> !s.isEmpty(),
                "A string não pode ser vazia"
        );
    }
    /**
     * Validador que verifica se o valor é menor que o limite especificado
     * @param limite Limite para comparação
     * @param <T> Tipo do valor (deve implementar Comparable)
     * @return Validator configurado
     */

    /**
     * Validador que verifica se o valor é menor que o limite especificado
     * @param limite Limite para comparação
     * @param <T> Tipo do valor (deve implementar Comparable)
     * @return Validator configurado
     */

    public static Validator<LocalDate> val_data_futura() {
        return Validator.of(
                date -> date.isAfter(LocalDate.now()),
                "A data deve ser futura"
        );
    }
    /*
        * Validador que verifica se a data é passada (anterior a hoje)
     */
    public static Validator<LocalDate> val_data_passada() {
        return Validator.of(
                date -> date.isBefore(LocalDate.now()),
                "A data deve ser passada"
        );
    }

    public static <T extends Number & Comparable<T>> Validator<T> val_num_positivo() {
        return Validator.of(
                n -> n.doubleValue() > 0,
                "O número deve ser positivo"
        );
    }


    public static <T extends CharSequence> Validator<T> val_tam_min(int min) {
        return Validator.of(
                s -> s.length() >= min,
                String.format("Deve ter no mínimo %d caracteres", min)
        );
    }


    public static Validator<String> val_regex(String regex) {
        return Validator.of(
                s -> s.matches(regex),
                "Não corresponde ao padrão requerido"
        );
    }

    public static <T extends Comparable<T>> Validator<List<T>> val_lista_ordenada(boolean crescente) {
        return Validator.of(
                list -> {
                    for (int i = 0; i < list.size() - 1; i++) {
                        int cmp = list.get(i).compareTo(list.get(i + 1));
                        if (crescente ? cmp > 0 : cmp < 0) {
                            return false;
                        }
                    }
                    return true;
                },
                crescente ? "A lista deve estar em ordem crescente" : "A lista deve estar em ordem decrescente"
        );
    }
    public static <T> Validator<List<T>> val_lista_nenhum_atende(Predicate<T> condicao, String msgErro) {
        return Validator.of(
                list -> list.stream().noneMatch(condicao),
                msgErro
        );
    }
    public static <T> Validator<List<T>> val_lista_algum_atende(Predicate<T> condicao, String msgErro) {
        return Validator.of(
                list -> list.stream().anyMatch(condicao),
                msgErro
        );
    }

    public static <T> Validator<List<T>> val_lista_todos_atendem(Predicate<T> condicao, String msgErro) {
        return Validator.of(
                list -> list.stream().allMatch(condicao),
                msgErro
        );
    }
    public static <T> Validator<List<T>> val_lista_sem_nulos() {
        return Validator.of(
                list -> list.stream().noneMatch(Objects::isNull),
                "A lista não pode conter elementos nulos"
        );
    }

    public static <T> Validator<List<T>> val_lista_tam_exato(int tamanho) {
        return Validator.of(
                list -> list.size() == tamanho,
                String.format("A lista deve ter exatamente %d elementos", tamanho)
        );
    }
    public static <T> Validator<List<T>> val_lista_tam_max(int max) {
        return Validator.of(
                list -> list.size() <= max,
                String.format("A lista não pode ter mais que %d elementos", max)
        );
    }
    public static <T> Validator<List<T>> val_lista_tam_min(int min) {
        return Validator.of(
                list -> list.size() >= min,
                String.format("A lista deve ter no mínimo %d elementos", min)
        );
    }
    public static <T> Validator<List<T>> val_lista_nao_nula() {
        return Validator.of(
                Objects::nonNull,
                "A lista não pode ser nula"
        );
    }

    /**
     * Valida se o maior elemento da lista é maior que o valor especificado
     * @param x Valor de referência para comparação
     * @param <T> Tipo de elementos da lista (deve implementar Comparable)
     * @return Validador configurado
     */
    public static <T extends Comparable<T>> Validator<List<T>> val_lista_max_maior_que(T x) {
        return Validator.of(
                list -> {
                    if (list == null || list.isEmpty()) return false;
                    T max = Collections.max(list);
                    return max.compareTo(x) > 0;
                },
                String.format("O maior elemento da lista deve ser maior que %s", x)
        );
    }

    /**
     * Valida se o menor elemento da lista é menor que o valor especificado
     * @param y Valor de referência para comparação
     * @param <T> Tipo de elementos da lista (deve implementar Comparable)
     * @return Validador configurado
     */
    public static <T extends Comparable<T>> Validator<List<T>> val_lista_min_menor_que(T y) {
        return Validator.of(
                list -> {
                    if (list == null || list.isEmpty()) return false;
                    T min = Collections.min(list);
                    return min.compareTo(y) < 0;
                },
                String.format("O menor elemento da lista deve ser menor que %s", y)
        );
    }

    @SafeVarargs
    public static <T> Validator<T> val_compor(Validator<T>... validadores) {
        return value -> {
            List<ErrosValidacao> consolidado = new ArrayList<>();
            for (Validator<T> validador : validadores) {
                List<ErrosValidacao> resultado = validador.validar(value);
                if (!resultado.isEmpty()) {
                    consolidado.addAll(resultado);

                }
            }
            return consolidado;
        };
    }
    public static <T extends Comparable<T>> Validator<List<T>> val_lista_intervalo_extremos(T minReferencia, T maxReferencia) {
        return Validator.of(
                list -> {
                    if (list == null || list.isEmpty()) return false;

                    T min = list.getFirst();
                    T max = list.getFirst();

                    for (T item : list) {
                        if (item.compareTo(min) < 0) min = item;
                        if (item.compareTo(max) > 0) max = item;
                    }

                    return min.compareTo(minReferencia) < 0 && max.compareTo(maxReferencia) > 0;
                },
                String.format("Elementos devem ter mínimo < %s e máximo > %s", minReferencia, maxReferencia)
        );
    }
    /**
   * Valida se os elementos de uma lista estão ordenados conforme a prioridade definida
   * @param <T> Tipo dos elementos da lista
   */
    public static <T> Validator<List<T>> val_lista_ordem_prioridade(Comparator<T> comparadorPrioridade) {
        Objects.requireNonNull(comparadorPrioridade, "Comparador de prioridade não pode ser nulo");

        return Validator.of(
                lista -> {
                    if (lista == null || lista.isEmpty()) {
                        return true; // Lista vazia tecnicamente está em ordem
                    }

                    T anterior = lista.get(0);
                    for (int i = 1; i < lista.size(); i++) {
                        T atual = lista.get(i);
                        if (comparadorPrioridade.compare(anterior, atual) > 0) {
                            return false; // Prioridade violada
                        }
                        anterior = atual;
                    }
                    return true;
                },
                "A lista não está na ordem de prioridade requerida"
        );
    }
    public static <T> Validator<List<T>> val_lista_ordem_prioridade(
            Comparator<T> comparadorPrioridade,
            String mensagemErro
    ) {
        Objects.requireNonNull(comparadorPrioridade, "Comparador não pode ser nulo");
        Objects.requireNonNull(mensagemErro, "Mensagem de erro não pode ser nula");

        return Validator.of(
                lista -> {
                    if (lista == null || lista.isEmpty()) return true;

                    T anterior = lista.get(0);
                    for (int i = 1; i < lista.size(); i++) {
                        T atual = lista.get(i);
                        if (comparadorPrioridade.compare(anterior, atual) > 0) {
                            return false;
                        }
                        anterior = atual;
                    }
                    return true;
                },
                mensagemErro
        );
    }
    public static <T extends Comparable<T>> Validator<List<T>> val_lista_ordem_natural() {
        return val_lista_ordem_prioridade((Comparator<T>) Comparator.naturalOrder().reversed());
    }

    /**
     * Valida se a data é anterior à data atual com offset
     * @param dias Offset em dias
     * @return Validador configurado
     */
    public static Validator<LocalDate> val_data_antes_de(int dias) {
        return Validator.of(
                data -> {
                    Objects.requireNonNull(data, "Data não pode ser nula");
                    return data.isBefore(LocalDate.now().plusDays(dias));
                },
                String.format("A data deve ser anterior a %d dias a partir de hoje", dias)
        );
    }
    /**
     * Valida se a data é posterior à data atual com offset
     * @param dias Offset em dias
     * @return Validador configurado
     */
    public static Validator<LocalDate> val_data_depois_de(int dias) {
        return Validator.of(
                data -> {
                    Objects.requireNonNull(data, "Data não pode ser nula");
                    return data.isAfter(LocalDate.now().minusDays(dias));
                },
                String.format("A data deve ser posterior a %d dias antes de hoje", dias)
        );
    }

    /**
     * Valida se a data está dentro de um período relativo à data atual
     * @param diasAntes Número de dias antes de hoje
     * @param diasDepois Número de dias depois de hoje
     * @return Validador configurado
     */
    public static Validator<LocalDate> val_data_no_periodo(int diasAntes, int diasDepois) {
        return Validator.of(
                data -> {
                    Objects.requireNonNull(data, "Data não pode ser nula");
                    LocalDate inicio = LocalDate.now().minusDays(diasAntes);
                    LocalDate fim = LocalDate.now().plusDays(diasDepois);
                    return !data.isBefore(inicio) && !data.isAfter(fim);
                },
                String.format("A data deve estar entre %d dias antes e %d dias depois de hoje",
                        diasAntes, diasDepois)
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


