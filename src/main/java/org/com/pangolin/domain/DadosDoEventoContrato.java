package org.com.pangolin.domain;

import java.util.List;

public record DadosDoEventoContrato (
        String numeroDoContrato,
        List<ParcelaDoEventoContrato> parcelas){
}
