package org.com.pangolin.carteira.inicializacao.eventos.entrada;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a parcela installment for a contract event.
 *
 * @param id The unique identifier for the installment.
 * @param valor The amount of the installment.
 * @param dataVencimento The due date for the installment payment.
 */
public record ParcelaDoEventoContrato(
        String  id,
        BigDecimal valor,
        LocalDate  dataVencimento) {
}
