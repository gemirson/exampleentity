package org.com.pangolin.domain;

import org.com.pangolin.domain.core.validacoes.ErrosValidacao;
import org.com.pangolin.domain.core.validacoes.ResultadoValidacao;

import java.util.List;

public class CarteiraValidacaoService {

    private   ResultadoValidacao resultadoValidacao = ResultadoValidacao.validar();
    protected CarteiraValidacaoService(){resultadoValidacao = ResultadoValidacao.validar();}
    public CarteiraValidacaoService adicionarValidacao(String campo, List<ErrosValidacao> erros) {
        if (erros == null) {
            throw new IllegalArgumentException("A validação não pode ser nula.");
        }
        if(campo == null || campo.isEmpty()) {
            throw new IllegalArgumentException("O campo não pode ser nulo ou vazio.");
        }
        if  (erros.isEmpty()) {
            return this;
        }
        resultadoValidacao= resultadoValidacao.combinar(ResultadoValidacao.invalidar(campo, erros));
        return this;
    }

    public  static  CarteiraValidacaoService criar() {
        return new CarteiraValidacaoService();
    }
    public ResultadoValidacao validacao() {
        return resultadoValidacao;
    }


}
