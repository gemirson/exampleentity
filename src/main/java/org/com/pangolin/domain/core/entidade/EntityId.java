package org.com.pangolin.domain.core.entidade;


/**
 * An abstract base class for entity identifier objects that provides common identity behavior.
 *
 * <p>This class serves as a strongly-typed wrapper around identifier values, enforcing
 * non-null constraints and providing consistent equality, hashing, and string representation
 * based on the wrapped value.</p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * public class UserId extends EntityId<Long> {
 *     public UserId(Long value) {
 *         super(value);
 *     }
 * }
 *
 * UserId userId = new UserId(123L);
 * }</pre>
 *
 * @param <T> the type of the wrapped identifier value (e.g., Long, String, UUID)
 */
public abstract class EntityId<T> {
    private final T value;

    /**
     * Constructs a new EntityId with the specified value.
     *
     * @param value the identifier value (must not be null)
     * @throws IllegalArgumentException if the value is null
     */
    protected EntityId(T value) {
        if (value == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        this.value = value;
    }

    /**
     * Returns the wrapped identifier value.
     *
     * @return the non-null identifier value
     */
    public T getValue() {
        return value;
    }

    /**
     * Compares this EntityId with another object for equality.
     *
     * <p>Two EntityIds are considered equal if they are of the same runtime class
     * and their wrapped values are equal.</p>
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityId<?> entityId = (EntityId<?>) o;
        return value.equals(entityId.value);
    }

    /**
     * Returns a hash code value for this EntityId.
     *
     * @return a hash code value based on the wrapped value
     */
    @Override
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * Returns a string representation of this EntityId.
     *
     * <p>The string representation is equivalent to the string representation
     * of the wrapped value.</p>
     *
     * @return a string representation of the wrapped value
     */
    @Override
    public String toString() {
        return value.toString();
    }
}

