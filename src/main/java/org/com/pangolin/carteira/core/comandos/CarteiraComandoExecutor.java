package org.com.pangolin.carteira.core.comandos;

import org.com.pangolin.carteira.core.validacoes.ResultadoOuErro;
import org.com.pangolin.carteira.core.validacoes.ResultadoValidacao;

import java.util.Objects;
import java.util.function.Function;

public abstract class CarteiraComandoExecutor <Resposta, Entrada>  {

    public abstract ResultadoValidacao validarBusinessRules(Entrada command);

    public abstract Resposta executar(Entrada command);

    public static <Resposta, Entrada> ResultadoOuErro<ResultadoValidacao, Resposta> executarCarteiraValidada(
            Entrada entrada,
            Function<Entrada, ResultadoValidacao> validador,
            Function<Entrada, Resposta> operacao) {

        Objects.requireNonNull(entrada, "Entrada não pode ser nula");
        Objects.requireNonNull(validador, "Validador não pode ser nulo");
        Objects.requireNonNull(operacao, "Operação não pode ser nula");

        ResultadoValidacao validacao = validador.apply(entrada);
        if (!validacao.valido()) {
            return ResultadoOuErro.esquerdo(validacao);
        }

        try {
            Resposta resultado = operacao.apply(entrada);
            return ResultadoOuErro.direito(resultado);
        } catch (Exception e) {
            throw   new RuntimeException(e.getMessage());

        }
    }
}
