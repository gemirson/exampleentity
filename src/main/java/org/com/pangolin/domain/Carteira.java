package org.com.pangolin.domain;

import org.com.pangolin.domain.core.entidade.Entity;
import org.com.pangolin.domain.core.validacoes.RecordValidado;
import org.com.pangolin.domain.core.validacoes.ResultadoOuErro;
import org.com.pangolin.domain.core.validacoes.ResultadoValidacao;
import org.com.pangolin.domain.core.validacoes.Validacoes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a wallet (Carteira) entity in the domain model.
 *
 * <p>This class encapsulates the properties and behaviors of a wallet,
 * including its identifier, associated installments, and validation logic.</p>
 *
 * <p>It provides methods to create a new wallet based on contract event data
 * and to validate the wallet's properties.</p>
 */
public class Carteira extends Entity<String,CarteiraId> {

    /**
     * Represents a wallet (Carteira) entity in the domain model.
     *
     * <p>This class encapsulates the properties and behaviors of a wallet,
     * including its identifier, associated installments, and validation logic.</p>
     *
     * <p>It provides methods to create a new wallet based on contract event data
     * and to validate the wallet's properties.</p>
     */
    private ResultadoValidacao validacao;

    private CarteiraId    carteiraId;
    private List<Parcela> parcelas =  new ArrayList<Parcela>();

    private  static  final  CarteiraValidacaoService validacaoService = CarteiraValidacaoService.criar();


    /**
     * Constructs a new entity with the specified identifier.
     *
     * @param id the entity identifier (must not be null)
     * @throws IllegalArgumentException if the id is null
     */
    protected Carteira(Builder builder) {
        super(Objects.requireNonNull(builder.id,"ID naõ pode ser nulo"));
        this.carteiraId = getId();


    }

    private  ResultadoValidacao validarCarteira(DadosDoEventoContrato dados){

        validacaoService.adicionarValidacao("PARCELAS",
                                                    RecordValidado.validar(dados.parcelas(), Validacoes.listaNaoVazia()))
                        .adicionarValidacao("NUMERO_DO_CONTRATO",
                                                    RecordValidado.validar(dados.numeroDoContrato(), Validacoes.carteiraId(dados.numeroDoContrato())));

        return  validacaoService.validacao();




    }
    /** * Opens a new wallet (Carteira) based on the provided contract event data.
     *
     * <p>This method validates the contract event data and initializes the wallet with
     * the specified contract number and associated installments.</p>
     *
     * @param dadosDoEventoContrato the contract event data containing wallet details
     * @return the newly created Carteira instance validated against the provided data
     * @throws IllegalArgumentException if the contract event data is invalid
     */
    public ResultadoOuErro<ResultadoValidacao,Carteira> aberturaCarteira(DadosDoEventoContrato dadosDoEventoContrato) throws ResultadoValidacao.ValidacaoException {

        Objects.requireNonNull(dadosDoEventoContrato, "Dados do evento contrato não podem ser nulos");

        ResultadoValidacao validacao = validarCarteira(dadosDoEventoContrato);
        return  validacao.valido()
                ? ResultadoOuErro.direito(criarCarteiraComDados(dadosDoEventoContrato))
                : ResultadoOuErro.esquerdo(validacao);


    }

    /**
     * Creates a new Carteira instance with the specified contract event data.
     *
     * <p>This method initializes the wallet with the contract number and associated installments.</p>
     *
     * @param dadosDoEventoContrato the contract event data containing wallet details
     * @return a new Carteira instance populated with the provided data
     */
    private Carteira criarCarteiraComDados(DadosDoEventoContrato dadosDoEventoContrato) {
        List<Parcela> parcelas = criarParcelas(dadosDoEventoContrato.parcelas());
        CarteiraId carteiraId =  new CarteiraId(dadosDoEventoContrato.numeroDoContrato());
        return criarCarteira()
                .comId(carteiraId)
                .comParcelas(parcelas)
                .build();

    }
    /**
     * Creates a list of Parcelas (installments) based on the provided contract event data.
     *
     * <p>This method maps each installment data to a Parcela entity.</p>
     *
     * @param dadosParcelas the list of installment data to be converted
     * @return a list of Parcela entities created from the provided data
     */
    private List<Parcela> criarParcelas(List<ParcelaDoEventoContrato> dadosParcelas) {
        return dadosParcelas.stream()
                .map(d -> Parcela
                        .criarParcela()
                        .comId(new ParcelaId(d.id()))
                        .comValor(d.valor())
                        .comDataVencimento(d.dataVencimento())
                        .build())
                .toList();
    }





     /**
     * Creates a new Builder instance for constructing a Carteira entity.
     *
     * <p>This method provides a fluent interface for building Carteira objects
     * with required fields, ensuring that the ID is set before building.</p>
     *
     * @return a new Builder instance for Carteira
     */

    public static  Builder criarCarteira() {
        return  new Builder();
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


    /**
     * Builder class for constructing Carteira instances.
     *
     * <p>This class provides a fluent interface for setting the required fields
     * of a Carteira entity, ensuring that the ID is set before building.</p>
     */
    public static  class Builder {
        private CarteiraId id;
        private List<Parcela> parcelas;

        public Builder comId(CarteiraId id) {
            this.id = id;
            return this;
        }
        /**
         * Sets the list of installments (parcelas) for the wallet.
         *
         * @param parcelas the list of installments to associate with the wallet
         * @return this Builder instance for method chaining
         */
        public Builder comParcelas(List<Parcela> parcelas) {
            if (parcelas != null) {
                this.parcelas = parcelas;
            } else {
                this.parcelas = new ArrayList<>();
            }
            return this;
        }
        /**
         * Builds a new Carteira instance with the specified ID.
         *
         * @return a new Carteira instance
         * @throws IllegalArgumentException if the ID is null
         */
        public Carteira build() {
            if (id == null) {
                throw new IllegalArgumentException("O ID carteira não pode ser nulo");
            }
            return new Carteira(this);
        }
    }

}
