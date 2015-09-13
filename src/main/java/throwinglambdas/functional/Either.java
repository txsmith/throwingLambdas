package throwinglambdas.functional;

import java.util.Objects;
import java.util.Optional;

/**
 * An object that can have either in the left or right a value.
 *
 * @param <L> The type of value in the left component.
 * @param <R> The type of value in the right component.
 */
public class Either<L, R> {

    /**
     * Optional that might be storing the left value.
     */
    private Optional<L> l;

    /**
     * Optional that might be storing the right value.
     */
    private Optional<R> r;

    /**
     * Don't instantiate directly.
     */
    private Either() { /* Should never be used. */ }

    /**
     * Don't instantiate directly. Instead use {@link #left(Object)} or {@link #right(Object)}.
     *
     * @param l The optional for the left component.
     * @param r The optional for the right component.
     */
    private Either(Optional<L> l, Optional<R> r) {
        this.l = l;
        this.r = r;
    }

    /**
     * Create an either with a left component.
     *
     * @param l The value to store in the left component.
     * @param <L> The type of value in the left component.
     * @param <R> The type of value in the right component.
     * @return An either storing the value in the left component.
     */
    public static <L, R> Either<L, R> left(L l) {
        Objects.requireNonNull(l);
        return new Either<>(
                Optional.of(l),
                Optional.empty()
        );
    }

    /**
     * Create an either with a right component.
     *
     * @param r The value to store in the right component.
     * @param <L> The type of value in the left component.
     * @param <R> The type of value in the right component.
     * @return An either storing the value in the right component.
     */
    public static <L, R> Either<L, R> right(R r) {
        Objects.requireNonNull(r);
        return new Either<>(
                Optional.empty(),
                Optional.of(r)
        );
    }

    /**
     * Check if this either stores a left value.
     *
     * @return Whether this either stores a left value.
     */
    public boolean isLeft() {
        return this.l.isPresent();
    }

    /**
     * Check if this either stores a right value.
     *
     * @return Whether this either stores a right value.
     */
    public boolean isRight() {
        return this.r.isPresent();
    }

    /**
     * Get the left value if it is present. Else throws a {@link IllegalStateException}.
     *
     * @return The value stored in the left component, if it exists.
     */
    public L getLeft() {
        if (this.l.isPresent()) {
            return l.get();
        }
        throw new IllegalStateException("Either is of type right.");
    }

    /**
     * Get the right value if it is present. Else throws a {@link IllegalStateException}.
     *
     * @return The value stored in the right component, if it exists.
     */
    public R getRight() {
        if (this.r.isPresent()) {
            return r.get();
        }
        throw new IllegalStateException("Either is of type left.");
    }
}
