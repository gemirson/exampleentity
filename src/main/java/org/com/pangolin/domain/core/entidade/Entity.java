package org.com.pangolin.domain.core.entidade;

/**
 * An abstract base class for domain entities that provides common identity management.
 *
 * <p>This class serves as the foundation for all domain entities in the system,
 * enforcing that each entity must have a non-null identifier of type {@code EntityId}.
 * The actual equality comparison and hash code implementation are left to concrete
 * subclasses to implement based on their specific domain rules.</p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * public class User extends Entity<Long, UserId> {
 *     private final String name;
 *
 *     public User(UserId id, String name) {
 *         super(id);
 *         this.name = name;
 *     }
 *
 *     // Implement equals/hashCode based on business identity
 *     @Override
 *     public boolean equals(Object o) {
 *         if (this == o) return true;
 *         if (!(o instanceof User)) return false;
 *         User other = (User) o;
 *         return getId().equals(other.getId());
 *     }
 *
 *     @Override
 *     public int hashCode() {
 *         return getId().hashCode();
 *     }
 * }
 * }</pre>
 *
 * @param <T> the type of the underlying identifier value (e.g., Long, String, UUID)
 * @param <ID> the concrete {@link EntityId} subclass used as the entity's identifier
 */
public abstract class Entity<T, ID extends EntityId<T>> {
    private final ID id;

    /**
     * Constructs a new entity with the specified identifier.
     *
     * @param id the entity identifier (must not be null)
     * @throws IllegalArgumentException if the id is null
     */
    protected Entity(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        this.id = id;
    }

    /**
     * Returns the entity's identifier.
     *
     * @return the non-null entity identifier
     */
    public ID getId() {
        return id;
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
    public abstract boolean equals(Object o);

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
    public abstract int hashCode();
}
