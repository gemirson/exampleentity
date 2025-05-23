package org.com.pangolin.domain.core;

public record ErrosValidacao(String campos,String codigo,String menssagem
    )implements  RecordValidado {
    public ErrosValidacao {
        Validacoes.NAO_NULO_NEM_VAZIO.validar(campos);
        Validacoes.NAO_NULO_NEM_VAZIO.validar(codigo);
        Validacoes.NAO_NULO_NEM_VAZIO.validar(menssagem);
    }
    public  static Record criar(String campos, String codigo, String menssagem){
        return new ErrosValidacao(campos,codigo,menssagem);
    }
    @Override
    public String toString() {
        return "ErrosValidacao{" +
                "Campos='" + campos + '\'' +
                ", codigo='" + codigo + '\'' +
                ", menssagem='" + menssagem + '\'' +
                '}';
    }
}
