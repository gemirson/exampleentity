package org.com.pangolin.domain;


import org.com.pangolin.domain.core.Either;
import org.com.pangolin.domain.core.entidade.Entity;
import org.com.pangolin.domain.core.ValidationResult;

import java.math.BigDecimal;
import java.util.*;

/**
* Represents a wallet entity with balance, parcels, and validation status.
*
* <p>This immutable class provides operations for wallet management and
* enforces business rules through validation.</p>
*/
public  class Wallet extends Entity<String,WalletId> {
    private final BigDecimal balance;
    private List<Parcel> parcels = new ArrayList<>();
    private final ValidationResult validationResult;

    /**
     * Constructs a new Wallet instance with the specified parameters.
     *
     * <p>This protected constructor is designed for internal use and by subclasses.
     * Clients should use the factory method {@link #create(WalletId, List, BigDecimal)}
     * to ensure proper validation.</p>
     *
     * <p>The constructor initializes the wallet with:</p>
     * <ul>
     *   <li>A unique identifier</li>
     *   <li>The current balance amount</li>
     *   <li>A list of associated parcels</li>
     *   <li>The validation result of the wallet state</li>
     * </ul>
     *
     * @param id The unique identifier for this wallet (must not be {@code null})
     * @param balance The current balance of the wallet (may be {@code null} if not validated yet)
     * @param parcels The list of parcels associated with this wallet (may be {@code null} if not validated yet)
     * @param validationResult The validation state of this wallet (must not be {@code null})
     *
     * @throws NullPointerException if either {@code id} or {@code validationResult} are {@code null}
     *
     * @see #create(WalletId, List, BigDecimal)
     * @see ValidationResult
     *
     * @implNote This constructor:
     * <ul>
     *   <li>Does not perform validation of parameters (validation should be done before construction)</li>
     *   <li>Does not make defensive copies of mutable parameters</li>
     *   <li>Should only be called with properly validated parameters</li>
     * </ul>
     *
     * @note For immutable wallet instances, the caller should ensure the {@code parcels} list
     *       is either immutable or won't be modified after construction.
     */
    protected Wallet(WalletId id, BigDecimal balance, List<Parcel> parcels, ValidationResult validationResult){
        super(id);
        this.balance = balance;
        this.validationResult = validationResult;
        this.parcels = parcels;
    }

    public void walletOpen(){
    }
    public void walletAmortization(){
    }
    public void walletAmortizationByCancel(){
    }
    public void walletRefund(){
    };

    public BigDecimal getBalance() {
        return balance;
    }

    public List<Parcel> getParcels() {
        return Collections.unmodifiableList(parcels);  // Retorna cópia imutável
    }

    /**
     * Factory method for creating a validated Wallet instance.
     *
     * <p>This is the preferred way to instantiate a Wallet as it performs complete validation
     * of all parameters before construction. The method validates:</p>
     * <ul>
     *   <li>That neither the ID nor parcels list are null</li>
     *   <li>That the balance meets all business rules</li>
     *   <li>That all parcels are valid</li>
     * </ul>
     *
     * <p>The method returns an {@link Either} monad that contains either:</p>
     * <ul>
     *   <li>A fully validated Wallet instance (right side), or</li>
     *   <li>A comprehensive ValidationResult with all detected errors (left side)</li>
     * </ul>
     *
     * <p><b>Usage Example:</b></p>
     * <pre>{@code
     * Either<ValidationResult, Wallet> walletOrErrors = Wallet.create(id, parcels, balance);
     *
     * walletOrErrors.fold(
     *     errors -> handleErrors(errors),
     *     wallet -> useValidWallet(wallet)
     * );
     * }</pre>
     *
     * @param id The unique identifier for the wallet (must not be null)
     * @param parcels The list of parcels associated with the wallet (must not be null, but can be empty)
     * @param balance The initial balance of the wallet (will be validated)
     *
     * @return An {@code Either} containing either the validation errors or a valid Wallet instance
     *
     * @throws NullPointerException if either {@code id} or {@code parcels} are null
     *
     * @see Either
     * @see ValidationResult
     * @see #validate(BigDecimal)
     *
     * @implNote The validation process:
     * <ol>
     *   <li>First validates basic null constraints</li>
     *   <li>Then validates the balance against business rules</li>
     *   <li>Finally validates each parcel in the list</li>
     *   <li>Combines all validation results before deciding whether to create the Wallet</li>
     * </ol>
     *
     * @apiNote For building wallets with more complex validation scenarios, consider:
     * <ul>
     *   <li>Using a {@code WalletBuilder} pattern</li>
     *   <li>Creating domain-specific validation methods</li>
     *   <li>Implementing custom validation rules</li>
     * </ul>
     */
    public static Either<ValidationResult, Wallet> create(WalletId id, List<Parcel> parcels, BigDecimal balance){

        Objects.requireNonNull(id, "Wallet ID cannot be null");
        Objects.requireNonNull(parcels, "Parcels list cannot be null");

        ValidationResult balanceValidation = validate(balance);
        ValidationResult parcelsValidation = parcels.stream()
                .map(Parcel::validate)
                .reduce(ValidationResult::combine)
                .orElse(ValidationResult.valid());


        ValidationResult finalValidation = balanceValidation.combine(parcelsValidation);

        return finalValidation.isValid()
                ? Either.right(new Wallet(id, balance, parcels, finalValidation))
                : Either.left(finalValidation);

    }

    /**
     * Validates a wallet balance against all business rules.
     *
     * <p>This private validation method checks the provided balance amount against
     * the following business rules:</p>
     * <ul>
     *   <li>The balance must not be null</li>
     *   <li>The balance must be ≥ {@link WalletConstants#MIN_ABSOLUTE_VALUE}</li>
     *   <li>The balance must be ≤ {@link WalletConstants#MAX_ALLOWED_BALANCE}</li>
     *   <li>The balance must be ≥ {@link WalletConstants#MIN_ALLOWED_BALANCE} (if applicable)</li>
     * </ul>
     *
     * <p>The method accumulates all validation failures in a structured error map
     * before converting them to a {@link ValidationResult}.</p>
     *
     * @param balance The balance amount to validate (may be null)
     * @return A {@code ValidationResult} indicating either:
     *         <ul>
     *           <li>{@code ValidationResult.valid()} if all rules pass</li>
     *           <li>An invalid result containing all validation errors otherwise</li>
     *         </ul>
     *
     * @see #balanceValidate(BigDecimal, Map)
     * @see WalletConstants
     * @see ValidationResult
     *
     * @implNote The validation:
     * <ul>
     *   <li>Uses a {@link LinkedHashMap} to preserve error order</li>
     *   <li>Short-circuits on null balance (adding just the null error)</li>
     *   <li>Checks all other rules sequentially when balance is non-null</li>
     *   <li>Groups all errors under {@link WalletConstants#BALANCE_KEY}</li>
     * </ul>
     *
     * @note This is an internal validation method called by {@link #create(WalletId, List, BigDecimal)}.
     *       Clients should use the public {@code create} method which provides complete validation.
     */
    private static ValidationResult validate(BigDecimal balance){
        Map<String, List<ValidationResult.ValidationError>> errors = new LinkedHashMap<>();
        balanceValidate(balance, errors);
        return errors.isEmpty()
                ? ValidationResult.valid()
                : ValidationResult.invalid(errors);
    }

    /**
     * Validates a wallet balance against business rules and accumulates any validation errors.
     *
     * <p>This method performs the following validations on the balance amount:</p>
     * <ol>
     *   <li><b>Null check</b>: Adds {@link WalletConstants#BALANCE_NOT_NULL_MESSAGE} if null</li>
     *   <li><b>Minimum absolute value</b>: Validates against {@link WalletConstants#MIN_ABSOLUTE_VALUE}</li>
     *   <li><b>Maximum allowed balance</b>: Validates against {@link WalletConstants#MAX_ALLOWED_BALANCE}</li>
     *   <li><b>Minimum allowed balance</b>: Validates against {@link WalletConstants#MIN_ALLOWED_BALANCE}</li>
     * </ol>
     *
     * <p>All validation errors are collected under the key {@link WalletConstants#BALANCE_KEY}
     * in the provided errors map.</p>
     *
     * @param balance The balance amount to validate (may be null)
     * @param errors A mutable map to accumulate validation errors (must not be null)
     *
     * @throws NullPointerException if the errors map is null
     *
     * @see WalletConstants
     * @see #validate(BigDecimal)
     *
     * @implSpec The validation:
     * <ul>
     *   <li>Short-circuits on null balance (only adds null error)</li>
     *   <li>Continues with all validations if balance is non-null</li>
     *   <li>Formats error messages with the actual limit values</li>
     *   <li>Preserves insertion order in the errors map</li>
     * </ul>
     *
     * @note The errors map is modified in-place. The caller is responsible for:
     * <ul>
     *   <li>Providing a mutable map instance</li>
     *   <li>Checking if the map contains errors after validation</li>
     *   <li>Handling the collected errors appropriately</li>
     * </ul>
     *
     * @warning The assignment {@code errors = balanceErrors} at the end has no effect
     *          as Java is pass-by-value. This should be removed as it's misleading.
     */
    private static void balanceValidate(BigDecimal balance, Map<String, List<ValidationResult.ValidationError>> errors ){

        List<ValidationResult.ValidationError> balanceErrors = new ArrayList<>();

        if (balance == null) {
            balanceErrors.add(
                     new ValidationResult.ValidationError.Builder("BALANCE_INVALID")
                    .withMessageKey( WalletConstants.BALANCE_NOT_NULL_MESSAGE)
                    .build());
        } else {
            if (balance.compareTo(WalletConstants.MIN_ABSOLUTE_VALUE) < 0) {
                balanceErrors.add(
                        new ValidationResult.ValidationError.Builder("BALANCE_INVALID")
                                .withMessageKey(String.format(WalletConstants.BALANCE_MARGIN_MIN_MESSAGE,
                                                WalletConstants.MIN_ABSOLUTE_VALUE))
                                .build());
            }
            if (balance.compareTo(WalletConstants.MAX_ALLOWED_BALANCE) > 0) {
                balanceErrors.add(
                        new ValidationResult.ValidationError.Builder("BALANCE_INVALID")
                                 .withMessageKey(String.format(WalletConstants.BALANCE_MARGIN_MAX_MESSAGE,
                                             WalletConstants.MAX_ALLOWED_BALANCE))
                                 .build());
            }
            if (balance.compareTo(WalletConstants.MIN_ALLOWED_BALANCE) < 0) {
                balanceErrors.add(
                        new ValidationResult.ValidationError.Builder("BALANCE_INVALID")
                                .withMessageKey(String.format(WalletConstants.BALANCE_MARGIN_MIN_MESSAGE,
                                             WalletConstants.MIN_ALLOWED_BALANCE))
                                .build());
            }
        }

        if (!balanceErrors.isEmpty()) {
            errors.put(WalletConstants.BALANCE_KEY, balanceErrors);
        }
    }

    public ValidationResult validate() {
        return validationResult;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wallet wallet = (Wallet) o;
        return Objects.equals(getId(), wallet.getId()) &&
                Objects.equals(balance, wallet.balance) &&
                Objects.equals(parcels, wallet.parcels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), balance, parcels);
    }

    @Override
    public String toString() {
       /** return new StringJoiner(", ", Wallet.class.getSimpleName() + "[", "]")
                .add(""\id\":" + getId().toString())
                .add(""\balance\":" + balance)
                .add(""\parcelsCount\":" + parcels.size())
                .add(""\valid\":" + validationResult.isValid())
                .toString();*/
       return "";
    }


}
