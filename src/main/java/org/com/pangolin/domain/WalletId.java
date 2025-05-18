package org.com.pangolin.domain;

import org.com.pangolin.domain.core.EntityId;

import java.util.Objects;

/**
 * A domain-specific identifier for Wallet entities, representing a unique identifier
 * as a String value.
 *
 * <p>This class extends {@link EntityId} to provide type safety and validation
 * for wallet identifiers throughout the application domain.</p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * // Creating a new WalletId
 * WalletId walletId = new WalletId("WALLET-12345-XYZ");
 *
 * // Using in Wallet entity construction
 * Wallet wallet = new Wallet(walletId, ...);
 * }</pre>
 *
 * @see EntityId
 */
public class WalletId extends EntityId<String> {

    /**
     * Constructs a new WalletId with the specified string value.
     *
     * @param value the unique identifier string for the wallet
     *              (must not be null or blank)
     * @throws IllegalArgumentException if the value is null or blank
     *
     * @implSpec This constructor:
     * <ul>
     *   <li>Delegates to {@link EntityId#EntityId(Object)} for basic validation</li>
     *   <li>Ensures the value meets wallet ID format requirements</li>
     * </ul>
     */
    protected WalletId(String value) {
        super(validateWalletId(value));
    }

    /**
     * Validates the wallet ID format.
     *
     * @param value the wallet ID to validate
     * @return the validated value
     * @throws IllegalArgumentException if validation fails
     *
     * @implNote Current validation rules:
     * <ul>
     *   <li>Value must not be null</li>
     *   <li>Value must not be blank</li>
     *   <li>Value must start with "WALLET-" prefix</li>
     *   <li>Value must be at least 10 characters long</li>
     * </ul>
     */
    private static String validateWalletId(String value) {
        Objects.requireNonNull(value, "Wallet ID cannot be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("Wallet ID cannot be blank");
        }

        if (!value.startsWith("WALLET-")) {
            throw new IllegalArgumentException("Wallet ID must start with 'WALLET-' prefix");
        }

        if (value.length() < 10) {
            throw new IllegalArgumentException("Wallet ID must be at least 10 characters long");
        }

        return value;
    }
}
