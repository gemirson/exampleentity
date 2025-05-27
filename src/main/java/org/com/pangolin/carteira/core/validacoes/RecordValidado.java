package org.com.pangolin.carteira.core.validacoes;

import java.util.List;

public interface RecordValidado {
     ResultadoValidacao resultadoValidacao= ResultadoValidacao.validar();
    /**
     * Método para validar um objeto do tipo T usando um validador específico.
     *
     * @param value     o objeto a ser validado
     * @param validator o validador a ser usado
     * @param <T>       o tipo do objeto a ser validado
     * @return uma lista de erros de validação, ou uma lista vazia se não houver erros
     */
    static <T> List<ErrosValidacao> validar(T value, Validator<T> validator) {
           return validator.validar(value);
    }

    /**
     * Método para validar um objeto do tipo T e lançar uma exceção se houver erros.
     *
     * @param value     o objeto a ser validado
     * @param validator o validador a ser usado
     * @param <T>       o tipo do objeto a ser validado
     * @throws ValidacaoException se a validação falhar e o usuário preferir tratamento por exceção
     */
    static <T> List<T> validarLista(List<T> lista, Validator<List<T>> validator) {
        validar(lista, validator);
        return List.copyOf(lista); // Retorna lista imutável
    }

   /**
     * Método para validar uma string usando um ou mais validadores.
     *
     * @param value       a string a ser validada
     * @param validators  os validadores a serem aplicados
     */
    static void validarString(String value,
                                        Validator<? super String>... validators) {
        Validator<String> combined = Validator.of(v -> true, "");
        for (Validator<? super String> v : validators) {
            combined = combined.and(v);
        }
        validar(value, combined);
    }

    /**
     * Cria um objeto de erro de validação com código e mensagem.
     * @param codigo
     * @param menssagem
     * @return
     */
     static ErrosValidacao  adicionarErroValidacao( String codigo, String menssagem) {
         return new ErrosValidacao(codigo, menssagem, false);
     }
     /*
      * Cria um objeto de erro de validação com código, mensagem e campo específico.
      */
     default RecordValidado adicionarResultadoValidacao(String campo, List<ErrosValidacao> erro) {
            if (erro == null || erro.isEmpty()) {
                return this;
            }
            resultadoValidacao.combinar(ResultadoValidacao.invalidar(campo, erro));
            return  this;
     }
}