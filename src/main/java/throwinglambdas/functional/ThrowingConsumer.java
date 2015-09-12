package throwinglambdas.functional;

/**
 * A function that accepts input of type {@code T} and might throw exceptions of type {@code E}.
 *
 * @param <T> The type of input.
 * @param <E> The type of exceptions thrown.
 */
@FunctionalInterface
public interface ThrowingConsumer<T, E extends Throwable> {

    /**
     * Execute the method.
     *
     * @param t The input to the method.
     * @throws E The exception that might be thrown.
     */
    void accept(T t) throws E;
}
