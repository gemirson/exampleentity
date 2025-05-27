package org.com.pangolin.carteira.inicializacao;

import org.com.pangolin.carteira.inicializacao.entidade.Carteira;
import org.com.pangolin.carteira.inicializacao.eventos.entrada.DadosDoEventoContrato;
import org.com.pangolin.carteira.core.comandos.CarteiraComandoExecutor;
import org.com.pangolin.carteira.core.validacoes.ResultadoOuErro;
import org.com.pangolin.carteira.core.validacoes.ResultadoValidacao;

import java.util.Objects;

public class AberturaCarteiraExecutor extends CarteiraComandoExecutor<Carteira, DadosDoEventoContrato> {

    /**
     * Método para validar as regras de negócio do comando de abertura de carteira.
     *
     * @param command Dados do evento de contrato.
     * @return Resultado da validação das regras de negócio.
     */
    @Override
    public ResultadoValidacao validarBusinessRules(DadosDoEventoContrato command) {
        // Implementar regras de negócio para validação do comando de abertura de carteira
        // Exemplo: Verificar se o número do contrato é válido, se as parcelas estão corretas, etc.
        // Se houver erros, lançar uma exceção ou adicionar erros ao resultado de validação.
        if (command == null) {
            throw new IllegalArgumentException("Dados do evento contrato não podem ser nulos");
        }
        return Carteira.validarCarteira(command);

    }

    /**
     * @param command
     * @return
     */
    @Override
    public Carteira executar(DadosDoEventoContrato command) {
        Objects.requireNonNull(command, "Dados do evento contrato não podem ser nulos");
        Carteira carteira = Carteira.criarCarteira().build();
        return carteira.aberturaCarteira(command).get();

    }

    /**
     * Método para processar o comando de abertura de carteira.
     *
     * @param command Dados do evento de contrato.
     * @return Resultado ou erro do processamento do comando.
     */
    public  ResultadoOuErro<ResultadoValidacao, Carteira> processar(DadosDoEventoContrato command) {
      /**
      * Método para aplicar o comando de abertura de carteira.
      *
      * @param command Dados do evento de contrato.
      */
      Objects.requireNonNull(command, "Dados do evento contrato não podem ser nulos");
      return  CarteiraComandoExecutor.executarCarteiraValidada(
               command,
               this::validarBusinessRules,
               carteiraComando -> executar(command)

      );

    }

    /**
     * Método para criar uma instância do executor de abertura de carteira.
     *
     * @return Uma nova instância de AberturaCarteiraExecutor.
     */
    public  static AberturaCarteiraExecutor criar() {
        return new AberturaCarteiraExecutor();
    }


}
