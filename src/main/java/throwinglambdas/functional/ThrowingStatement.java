package throwinglambdas.functional;

/**
 * A method that might throw exceptions of type {@code E}.
 *
 * @param <E> The type of exception.
 */
@FunctionalInterface
public interface ThrowingStatement<E extends Throwable> {

    /**
     * Execute the method.
     *
     * @throws E The exception that might be thrown.
     */
    void apply() throws E;
}
