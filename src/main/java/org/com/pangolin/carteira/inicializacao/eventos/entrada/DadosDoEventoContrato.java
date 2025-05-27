package org.com.pangolin.carteira.inicializacao.eventos.entrada;

import java.util.List;

public record DadosDoEventoContrato (
        String numeroDoContrato,
        List<ParcelaDoEventoContrato> parcelas){
}
