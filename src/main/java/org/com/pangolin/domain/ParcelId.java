package org.com.pangolin.domain;

import org.com.pangolin.domain.core.entidade.EntityId;

import java.util.Objects;

/**
 * A strongly-typed identifier for {@code Parcel} entities, using a {@code String} as the underlying value.
 *
 * <p>This class extends {@link EntityId} to provide domain-specific identity for parcels in the system.
 * The identifier is guaranteed to be non-null through the parent class validation.</p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * // Creating a new ParcelId
 * ParcelId parcelId = new ParcelId("PARCEL-12345");
 *
 * // Using as part of a Parcel entity
 * Parcel parcel = new Parcel(parcelId, ...);
 * }</pre>
 *
 * @see EntityId
 */
public class ParcelId extends EntityId<String> {
    /**
     * Constructs a new {@code ParcelId} with the specified string value.
     *
     * @param value the string representation of the parcel identifier (must not be {@code null} or empty)
     * @throws IllegalArgumentException if the value is {@code null} or empty
     *
     * @implNote The constructor delegates to {@link EntityId#EntityId(Object)} which enforces
     *           the non-null constraint. Additional validation can be added here if needed.
     */
    protected ParcelId(String value) {
        super(Objects.requireNonNull(value, "Parcel ID value cannot be null"));
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Parcel ID value cannot be empty");
        }
    }
}
