package throwinglambdas;

import com.google.common.collect.Lists;
import throwinglambdas.functional.Either;
import throwinglambdas.functional.Statement;
import throwinglambdas.functional.ThrowingConsumer;
import throwinglambdas.functional.ThrowingFunction;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * TODO: Add general explanation of usage of this library.
 */
public class ThrowingLambdas {

    /**
     * Don't instantiate this directly. Instead invoke all static methods.
     */
    private ThrowingLambdas() {

    }

    /**
     * Compute a function on all elements in a {@link Set}
     * and filter the results where that function returns {@link Optional#empty()}.
     * The result of this function may therefore be smaller than it's input.
     * <p>
     * Use with {@link #toOptional(ThrowingFunction)} for functions that might throw exceptions.
     *
     * @param set    The set to map.
     * @param mapper The partial function to use.
     * @param <T>    Type of elements in the input set.
     * @param <R>    Type of elements in the output set.
     * @return Resulting set of the mapping.
     */
    public static <T, R> Set<R> safeMap(Set<? extends T> set, Function<? super T, Optional<R>> mapper) {
        return safeMap(set, mapper, Collectors.toSet());
    }

    /**
     * Compute a function on all elements in a {@link List}
     * and filter the results where that function returns {@link Optional#empty()}.
     * The result of this function may therefore be smaller than it's input.
     * <p>
     * Use with {@link #toOptional(ThrowingFunction)} for functions that might throw exceptions.
     *
     * @param list    The list to map.
     * @param mapper The partial function to use.
     * @param <T>    Type of elements in the input list.
     * @param <R>    Type of elements in the output list.
     * @return Resulting set of the mapping.
     */
    public static <T, R> List<R> safeMap(List<? extends T> list, Function<? super T, Optional<R>> mapper) {
        return safeMap(list, mapper, Collectors.toList());
    }

    /**
     * Compute a function on all elements in a {@link Collection}
     * and filter the results where that function returns {@link Optional#empty()}.
     * The result of this function may therefore be smaller than it's input.
     * <p>
     * Use with {@link #toOptional(ThrowingFunction)} for functions that might throw exceptions.
     *
     * @param collection    The collection to map.
     * @param mapper The partial function to use.
     * @param collector The collector that returns the collection.
     * @param <T>    Type of elements in the input collection.
     * @param <R>    Type of elements in the output collection.
     * @return Resulting collection of the mapping.
     */
    public static <T, R, C> C safeMap(Collection<? extends T> collection, Function<? super T, Optional<R>> mapper, Collector<R, ?, C> collector) {
        return collection.stream()
                .map(mapper)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(collector);
    }

    /**
     * Turn a partial function into a total function.
     * The function is made total by returning {@link Optional#empty()}
     * when an exception is thrown.
     *
     * @param throwingFunction The partial function to transform.
     * @param <T>              Type of input arguments to that function.
     * @param <R>              Possible output type of that function.
     * @return A function from {@code T} to an {@link Optional}.
     */
    public static <T, R> Function<T, Optional<R>> toOptional(ThrowingFunction<T, R, ?> throwingFunction) {
        return t -> {
            try {
                return Optional.of(throwingFunction.apply(t));
            } catch (Throwable exception) {
                return Optional.empty();
            }
        };
    }

    /**
     * Turn a partial function into a total function.
     * The function is made total by returning {@link Either#left(Object)}
     * when an exception is thrown. The {@link Either#getLeft()} contains
     * the corresponding exception.
     *
     * @param throwingFunction The partial function to transform.
     * @param <T> Type of input arguments to that function.
     * @param <R> Possible output type of that function.
     * @return An function from {@code T} to an {@link Either}.
     */
    public static <T, R> Function<T, Either<? extends Throwable, R>> toEither(ThrowingFunction<T, R, ?> throwingFunction) {
        return t -> {
            try {
                return Either.right(throwingFunction.apply(t));
            } catch (Throwable exception) {
                return Either.left(exception);
            }
        };
    }

    /**
     * Silence the checked exceptions that are thrown by the {@link ThrowingConsumer}.
     *
     * @param consumer The consumer that throws checked exceptions.
     * @param <T> The type of the input to the consumer.
     * @return A consumer that does not throw checked exceptions.
     */
    public static <T> Consumer<T> silenceExceptions(ThrowingConsumer<T, ?> consumer) {
        return silenceExceptions(toThrowingFunction(consumer))::apply;
    }

    /**
     * Silence the checked exceptions that are thrown by the {@link ThrowingFunction}.
     *
     * @param function The function that throws checked exceptions.
     * @param <T> The type of the input to the function.
     * @param <R> The type of the output of the function.
     * @return A function that does not throw checked exceptions.
     */
    public static <T, R> Function<T, R> silenceExceptions(ThrowingFunction<T, R, ?> function) {
        return t -> {
            try {
                return function.apply(t);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * Transforms a {@link ThrowingConsumer} to a {@link ThrowingFunction}.
     *
     * <p><b>Note: The resulting function always returns  null.</b></p>
     *
     * @param consumer The consumer.
     * @param <T> The type of input to the consumer.
     * @param <E> The type of exception the consumer throws.
     * @return A function that throws exceptions.
     */
    public static <T, E extends Throwable> ThrowingFunction<T, Void, E> toThrowingFunction(ThrowingConsumer<T, E> consumer) {
        return (T t) -> { consumer.accept(t); return null; };
    }

    /**
     * Create a {@link throwinglambdas.ThrowingLambdas.TryFinally} which can be used to collect garbage
     * created in the provided consumer. The provided consumer will only be executed during
     * {@link throwinglambdas.ThrowingLambdas.TryFinally#with(Object)}.
     *
     * @param consumer The consumer that processes {@link throwinglambdas.ThrowingLambdas.Garbage}
     *                 and the input of {@link throwinglambdas.ThrowingLambdas.TryFinally#with(Object)}.
     * @param <T> The type of input for the consumer.
     * @return An object to call {@link throwinglambdas.ThrowingLambdas.TryFinally#with(Object)} on to
     *         generate and clean up garbage.
     */
    public static <T> TryFinally<T> using(GarbageThrowingConsumer<T, ?> consumer) {
        return new TryFinally<>(consumer);
    }

    /**
     * Object to capture a {@link throwinglambdas.ThrowingLambdas.GarbageThrowingConsumer},
     * process the input from {@link #with(Object)} and {@link Garbage#cleanup()} the garbage.
     *
     * @param <T> The type of input for the consumer.
     */
    public static class TryFinally<T> {

        /**
         * The consumer that produces garbage and might throw exceptions.
         */
        private GarbageThrowingConsumer<T,?> throwingConsumer;

        /**
         * The garbagecollector.
         */
        private Garbage garbage;

        /**
         * Don't instantiate directly. Instead rely on {@link #using(GarbageThrowingConsumer)}.
         *
         * @param throwingConsumer The consumer that generates garbage and might throw exceptions.
         */
        private TryFinally(GarbageThrowingConsumer<T, ?> throwingConsumer) {
            this.throwingConsumer = throwingConsumer;
            this.garbage = new Garbage();
        }

        /**
         * Invoke the {@link #throwingConsumer}, catch any exceptions and finally cleanup the generated garbage.
         *
         * @param t The input for the consumer.
         */
        public void with(T t) {
            try {
                this.throwingConsumer.accept(garbage, t);
            } catch (Throwable ignored) {
            } finally {
                garbage.cleanup();
            }
        }
    }

    /**
     * A consumer that takes input and generates garbage. This garbage can be added with
     * {@link throwinglambdas.ThrowingLambdas.Garbage#add(Object, Consumer)}.
     *
     * @param <T> The type of input for the consumer.
     * @param <E> The type of exception the consumer can throw.
     */
    public interface GarbageThrowingConsumer<T, E extends Throwable> {
        void accept(Garbage garbage, T t) throws E;
    }

    /**
     * Garbage collector that collects {@link Consumer} and processes them during {@link #cleanup()}.
     */
    public static class Garbage {

        /**
         * The list to process in order to clean up garbage.
         */
        private List<Statement> garbages;

        /**
         * Don't instantiate directly. Instead rely on {@link #using(GarbageThrowingConsumer)}.
         */
        private Garbage() {
            this.garbages = Lists.newArrayList();
        }

        /**
         * Add garbage to the list and provide the clean up method as {@link Consumer}.
         *
         * @param t The garbage generated.
         * @param consumer The clean up method.
         * @param <T> The type of garbage generated.
         */
        public <T> void add(T t, Consumer<T> consumer) {
            garbages.add(() -> consumer.accept(t));
        }

        /**
         * Clean up all the garbage generated.
         */
        public void cleanup() {
            garbages.forEach(Statement::apply);
        }
    }
}
