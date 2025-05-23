package org.com.pangolin.domain.core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ResultadoValidacao {

    private  final  boolean valido;
    private Map<String, List<ErrosValidacao>> errosValidacao;

    public ResultadoValidacao(boolean valido, Map<String, List<ErrosValidacao>> errosValidacao) {
        this.valido = valido;
        this.errosValidacao = errosValidacao;
    }

    public boolean isValido() {
        return valido;
    }

    public Map<String, List<ErrosValidacao>> getErrosValidacao() {
        return errosValidacao;
    }
    public void setErrosValidacao(Map<String, List<ErrosValidacao>> errosValidacao) {
        this.errosValidacao = errosValidacao;
    }

    public static  ResultadoValidacao invalidar(Map<String, List<ErrosValidacao>> errosValidacao) {
        return new ResultadoValidacao(false, errosValidacao);
    }

    public  static ResultadoValidacao validar() {
        return new ResultadoValidacao(true, null);
    }
    public  static ResultadoValidacao invalidar(String campo, String codigo, String menssagem) {
        return invalidar(Map.of(campo, List.of(new ErrosValidacao(campo, codigo, menssagem))));
    }

    public  static ResultadoValidacao invalidar(String campo, List<ErrosValidacao> errosValidacao) {
        return invalidar(Map.of(campo, errosValidacao));
    }

    public ResultadoValidacao combinar(ResultadoValidacao outro) {
        if (this.valido && outro.valido) {
            return validar();
        }
        return new ResultadoValidacao(false, mergeErrors(this.errosValidacao, outro.errosValidacao));
    }
    public static ResultadoValidacao criar(boolean valido, Map<String, List<ErrosValidacao>> errosValidacao) {
        return new ResultadoValidacao(valido, errosValidacao);
    }
    /**
     *
     * @param map1
     * @param map2
     * @return
     */
    private static Map<String, List<ErrosValidacao>> mergeErrors(
            Map<String, List<ErrosValidacao>> errosValidacao,
            Map<String, List<ErrosValidacao>> outroErrosValidacao) {

        Map<String, List<ErrosValidacao>> merged = new LinkedHashMap<>();
        Stream.of(errosValidacao, outroErrosValidacao).forEach(map ->
                map.forEach((key, value) -> {
                    merged.merge(key, value, (oldVal, newVal) -> {
                        oldVal.addAll(newVal);
                        return oldVal;
                    });
                }));
        return merged;
    }
}
