package org.com.pangolin.carteira.core.validacoes;

public record ErrosValidacao(String codigo, String menssagem,
                             boolean deveLancarExcecao)implements RecordValidado {
    public ErrosValidacao {

        Validacoes.NAO_NULO_NEM_VAZIO.validar(codigo);
        Validacoes.NAO_NULO_NEM_VAZIO.validar(menssagem);
    }
    public  static Record criar(String codigo, String menssagem, boolean deveLancarExcecao){
        return new ErrosValidacao(codigo,menssagem,deveLancarExcecao);
    }



    @Override
    public String toString() {
        return "ErrosValidacao{" +

                "codigo='" + codigo + '\'' +
                ", mensagem='" + menssagem + '\'' +
                '}';
    }
}
