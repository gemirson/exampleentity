package org.com.pangolin.domain.core;

import java.math.BigDecimal;
import java.util.List;

import static org.com.pangolin.domain.core.RecordValidado.validar;

public record Transacao(
        String id,
        BigDecimal valor,
        List<Transacao> transacoesSimultaneas
) implements RecordValidado{
    public Transacao {
        Validacoes.naoNuloENaoVazio().validar(id);
        validar(valor, Validacoes.MAIOR_QUE_ZERO);

        // Verifica se não há outra transação com mesmo ID
        validar(this,
                Validacoes.nenhumAtende(
                        transacoesSimultaneas,
                        t -> t.id().equals(id),
                        "Já existe uma transação com o mesmo ID"
                )
        );
    }
}
