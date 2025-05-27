package org.com.pangolin.carteira.inicializacao.entidade;

import org.com.pangolin.carteira.core.entidade.Entity;
import org.com.pangolin.carteira.core.validacoes.RecordValidado;
import org.com.pangolin.carteira.core.validacoes.Validator;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Parcela extends Entity<String, ParcelaId> {

    private static final long serialVersionUID = 1L;
    /**
     * Represents a financial installment (Parcela) in a contract.
     * Each installment has an identifier, value, due date, and tracks the
     * amount of debt and amortization.
     * This fiels is imutable and  not initialized to zero.
     */
    private final ParcelaId  id;
    private final BigDecimal valor;
    private final LocalDate  dataVencimento;

    /** Represents the amount of debt remaining for this installment.
     * This field is mmutable and initialized to zero.
     */
    private final BigDecimal valorSaldoDevedor = BigDecimal.ZERO;
    private final BigDecimal valorSaldoAmortizado = BigDecimal.ZERO;// Assuming a default value for saldo

    /**
     * Constructs a new entity with the specified identifier.
     *
     * @param builder
     * @throws IllegalArgumentException if the id is null
     */
    protected Parcela( Builder builder) {
        super(builder.id);
        this.id = getId();
        this.valor = builder.valor;
        this.dataVencimento = builder.dataVencimento;

    }

    /**
     * Indicates whether some other object is "equal to" this entity.
     *
     * <p>Concrete subclasses must implement this method according to their
     * business identity rules. Typically, entities are considered equal
     * if they have the same ID and are of the same class.</p>
     *
     * @param o the reference object with which to compare
     * @return true if this object is the same as the obj argument; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        return false;
    }

    /**
     * Returns a hash code value for the entity.
     *
     * <p>Concrete subclasses must implement this method consistent with their
     * equals implementation. The hash code should typically be derived from
     * the same fields used in equality comparison.</p>
     *
     * @return a hash code value for this entity
     */
    @Override
    public int hashCode() {
        return 0;
    }

    public static  Builder criarParcela() {
        return  new Builder();
    }


    public  static final class Builder {
        private ParcelaId id;
        private BigDecimal valor;
        private LocalDate dataVencimento;

        public Builder comId(ParcelaId id) {
            this.id = id;
            return this;
        }

        public Builder comValor(BigDecimal valor) {
            this.valor = valor;
            return this;
        }

        public Builder comDataVencimento(LocalDate dataVencimento) {
            this.dataVencimento = dataVencimento;
            return this;
        }
        /**
         * Builds a new Parcela instance with the provided parameters.
         *
         * @return a new Parcela instance
         * @throws IllegalArgumentException if any validation fails
         */

        public  Parcela build() {
            validar();
            return new Parcela(this);
        }

        /**
         * Validates the fields of the Parcela instance.
         *
         * @throws Validator.ValidacaoException if any validation fails
         */
        private void validar(){
            RecordValidado.validar(id, Validator.of(v-> v != null,"FATAL_ERROR" ,"O Id da Parcela não pode ser nulo",true));
            RecordValidado.validar(dataVencimento, Validator.of(v -> v != null, "FATAL_ERROR", "A data de vencimento da Parcela não pode ser nula", true));
            RecordValidado.validar(valor, Validator.of(v -> v != null && v.compareTo(BigDecimal.ZERO) > 0, "FATAL_ERROR", "O valor da Parcela não pode ser nulo ou menor ou igual a zero", true));
            RecordValidado.validar(valor, Validator.of(v -> v.scale() <= 2, "FATAL_ERROR", "O valor da Parcela deve ter no máximo duas casas decimais", true));
        }
    }
}
