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

public class ThrowingLambdas {

    /**
     * Compute a function on all elements in a {@link Set}
     * and filter the results where that function returns {@link Optional}{@code .empty()}.
     * The result of this function may therefore be smaller than it's input.
     * <p>
     * Use with {@code toOptional} for functions that might throw exceptions.
     *
     * @param set    The set to map.
     * @param mapper The partial function to use.
     * @param <T>    Type of elements in the input set
     * @param <R>    Type of elements in th output set.
     * @return Resulting set of the mapping.
     */
    public static <T, R> Set<R> safeMap(Set<? extends T> set, Function<? super T, Optional<R>> mapper) {
        return safeMap(set, mapper, Collectors.toSet());
    }

    public static <T, R> List<R> safeMap(List<? extends T> list, Function<? super T, Optional<R>> mapper) {
        return safeMap(list, mapper, Collectors.toList());
    }

    public static <T, R, C> C safeMap(Collection<? extends T> list, Function<? super T, Optional<R>> mapper, Collector<R, ?, C> collector) {
        return list.stream()
                .map(mapper)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(collector);
    }

    /**
     * Turn a partial function into a total function.
     * The function is made total by returning {@link Optional}{@code .empty()}
     * when an exception is thrown.
     *
     * @param throwingFunction The partial function to transform.
     * @param <T>              Type of input arguments to that function
     * @param <R>              Possible output type of the function.
     * @return
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

    public static <T, R, E extends Throwable> Function<T, Either<? extends Throwable, R>> toEither(ThrowingFunction<T, R, E> throwingFunction) {
        return t -> {
            try {
                return Either.right(throwingFunction.apply(t));
            } catch (Throwable exception) {
                return Either.left(exception);
            }
        };
    }

    public static <T, E extends Throwable> Consumer<T> silenceExceptions(ThrowingConsumer<T, E> consumer) {
        return silenceExceptions(toThrowingFunction(consumer))::apply;
    }

    public static <T, R, E extends Throwable> Function<T, R> silenceExceptions(ThrowingFunction<T, R, E> consumer) {
        return t -> {
            try {
                return consumer.apply(t);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static <T, E extends Throwable> ThrowingFunction<T, Void, E> toThrowingFunction(ThrowingConsumer<T, E> c) {
        return (T t) -> { c.accept(t); return null; };
    }

    public static <T> TryFinally<T> using(GarbageThrowingConsumer<T, ?> consumer) {
        return new TryFinally<>(consumer);
    }

    public static class TryFinally<T> {
        private GarbageThrowingConsumer<T,?> throwingConsumer;
        private Garbage garbage;

        public TryFinally(GarbageThrowingConsumer<T, ?> throwingConsumer) {
            this.throwingConsumer = throwingConsumer;
            this.garbage = new Garbage();
        }

        public void with(T t) {
            try {
                this.throwingConsumer.accept(garbage, t);
            } catch (Throwable ignored) {
            } finally {
                garbage.cleanup();
            }
        }
    }

    public interface GarbageThrowingConsumer<T, E extends Throwable> {
        void accept(Garbage garbage, T t) throws E;
    }

    public static class Garbage {

        private List<Statement> garbages;

        public Garbage() {
            this.garbages = Lists.newArrayList();
        }

        public <T> void add(T t, Consumer<T> consumer) {
            garbages.add(() -> consumer.accept(t));
        }

        public void cleanup() {
            garbages.forEach(Statement::apply);
        }
    }
}
