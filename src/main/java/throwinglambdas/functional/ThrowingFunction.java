package throwinglambdas.functional;

/**
 * A method that accepts input of type {@code T}, returns output of type {@code R}
 * and might throw exceptions of type {@code E}.
 *
 * @param <T> The type of input.
 * @param <R> The type of output.
 * @param <E> The type of exceptions thrown.
 */
@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Throwable> {

    /**
     * Execute the method.
     *
     * @param t The input to the method.
     * @return The output of the method.
     * @throws E The exception that might be thrown.
     */
    R apply(T t) throws E;
}
