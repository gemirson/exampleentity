package org.com.pangolin.carteira.inicializacao.entidade;


import org.com.pangolin.carteira.core.entidade.EntityId;
import org.com.pangolin.carteira.core.validacoes.RecordValidado;
import org.com.pangolin.carteira.core.validacoes.Validacoes;

public  class CarteiraId extends EntityId<String> {
    /**
     * Constructs a new entity with the specified identifier.
     *
     * @param id the entity identifier (must not be null)
     * @throws IllegalArgumentException if the id is null
     */
    protected CarteiraId(String id) {
        super(id);
        validarCarteiraId(id);
    }

    private static void validarCarteiraId(String id) {
        RecordValidado.validar(id,
                Validacoes.carteiraId("9999"));
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
}
