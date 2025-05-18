package org.com.pangolin.domain.core;

import java.util.Objects;
import java.util.function.Function;

/**
 * A functional programming construct that represents a value of one of two possible types (a disjoint union).
 *
 * <p>An {@code Either} is either a {@code left} (typically representing a failure case) or
 * a {@code right} (typically representing a success case), but never both. This class is
 * commonly used for error handling where you want to explicitly represent both success and
 * failure cases in the type system.</p>
 *
 * <p><b>Typical Usage:</b></p>
 * <pre>{@code
 * Either<ValidationError, Wallet> result = Wallet.create(...);
 *
 * if (result.isRight()) {
 *     Wallet wallet = result.getRight();
 *     // handle success case
 * } else {
 *     ValidationError error = result.getLeft();
 *     // handle error case
 * }
 *
 * // Or using functional style:
 * String message = result.fold(
 *     error -> "Error: " + error.getMessage(),
 *     wallet -> "Created wallet with balance: " + wallet.getBalance()
 * );
 * }</pre>
 *
 * @param <L> the type of the left value (typically represents an error/failure)
 * @param <R> the type of the right value (typically represents a success result)
 */
public final class Either<L, R> {
    private final L left;
    private final R right;

    /**
     * Constructs a new Either instance.
     *
     * @param left the left value (must be null if right is non-null)
     * @param right the right value (must be null if left is non-null)
     * @throws IllegalArgumentException if both left and right are non-null or both are null
     */
    private Either(L left, R right) {
        if ((left != null && right != null) || (left == null && right == null)) {
            throw new IllegalArgumentException("Either must contain either a left or right value, but not both");
        }
        this.left = left;
        this.right = right;
    }

    /**
     * Creates an Either containing a left value.
     *
     * @param <L> the left type
     * @param <R> the right type
     * @param value the left value to contain (cannot be null)
     * @return an Either containing the left value
     * @throws NullPointerException if the value is null
     */
    public static <L, R> Either<L, R> left(L value) {
        Objects.requireNonNull(value, "Left value cannot be null");
        return new Either<>(value, null);
    }

    /**
     * Creates an Either containing a right value.
     *
     * @param <L> the left type
     * @param <R> the right type
     * @param value the right value to contain (cannot be null)
     * @return an Either containing the right value
     * @throws NullPointerException if the value is null
     */
    public static <L, R> Either<L, R> right(R value) {
        Objects.requireNonNull(value, "Right value cannot be null");
        return new Either<>(null, value);
    }

    /**
     * Returns true if this Either contains a left value.
     *
     * @return true if left value is present, false otherwise
     */
    public boolean isLeft() {
        return left != null;
    }

    /**
     * Returns true if this Either contains a right value.
     *
     * @return true if right value is present, false otherwise
     */
    public boolean isRight() {
        return right != null;
    }

    /**
     * Gets the left value.
     *
     * @return the left value
     * @throws IllegalStateException if this Either contains a right value
     */
    public L getLeft() {
        if (!isLeft()) {
            throw new IllegalStateException("No left value present");
        }
        return left;
    }

    /**
     * Gets the right value.
     *
     * @return the right value
     * @throws IllegalStateException if this Either contains a left value
     */
    public R getRight() {
        if (!isRight()) {
            throw new IllegalStateException("No right value present");
        }
        return right;
    }

    /**
     * Applies one of two functions to the contained value.
     *
     * <p>If this is a left value, applies {@code leftFunc} to it. If this is a right value,
     * applies {@code rightFunc} to it. Returns the result of the function application.</p>
     *
     * @param <T> the return type of the functions
     * @param leftFunc the function to apply if this is a left value (cannot be null)
     * @param rightFunc the function to apply if this is a right value (cannot be null)
     * @return the result of applying the appropriate function
     * @throws NullPointerException if either function is null
     */
    public <T> T fold(Function<L, T> leftFunc, Function<R, T> rightFunc) {
        Objects.requireNonNull(leftFunc, "Left function cannot be null");
        Objects.requireNonNull(rightFunc, "Right function cannot be null");
        return isLeft() ? leftFunc.apply(left) : rightFunc.apply(right);
    }

}
