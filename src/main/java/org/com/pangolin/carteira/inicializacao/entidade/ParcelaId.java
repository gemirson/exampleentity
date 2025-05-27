package org.com.pangolin.carteira.inicializacao.entidade;

import org.com.pangolin.carteira.core.entidade.EntityId;
import org.com.pangolin.carteira.core.validacoes.RecordValidado;
import org.com.pangolin.carteira.core.validacoes.Validacoes;
import org.com.pangolin.carteira.core.validacoes.Validator;

public class ParcelaId extends EntityId<String> implements RecordValidado {

    private static final long serialVersionUID = 1L;
    private static final long ID_MAXIMO = 999999L;

    public ParcelaId(String id) {
        super(id);
        validarParcelaId(id);
    }
    public static ParcelaId of(String id) {
        return new ParcelaId(id);
    }

    private static void validarParcelaId(String id) {
        RecordValidado.validar(id,
                Validacoes.NAO_NULO_NEM_VAZIO.and(
                        Validator.of(v -> Long.parseLong(v) <= ID_MAXIMO,
                                "O ID da parcela deve ser um número menor ou igual a " + ID_MAXIMO)));
    }

}
