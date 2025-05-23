package org.com.pangolin.domain.core;

import org.com.pangolin.domain.core.validacoes.RecordValidado;
import org.com.pangolin.domain.core.validacoes.Validacoes;

import java.math.BigDecimal;
import java.util.List;

import static org.com.pangolin.domain.core.validacoes.RecordValidado.validar;

public record Transacao(
        String id,
        BigDecimal valor,
        List<Transacao> transacoesSimultaneas
) implements RecordValidado {
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
