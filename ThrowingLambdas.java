package nl.tudelft.bigdata.functional;


import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ThrowingLambdas {

    public static void main(String ... args) {
        process((Garbage garbage, Double num) -> {
            garbage.add(5, System.out::println);

            if (0.5 > num) {
                throw new IOException("");
            }

            garbage.add(6, System.out::println);
        }).using(0.0).cleanup();
    }

    public static <T, R> Set<R> safeMapping(Set<T> list, Function<T, Optional<R>> mapper) {
        return safeMapping(list, mapper, Collectors.toSet());
    }

    public static <T, R> List<R> safeMapping(List<T> list, Function<T, Optional<R>> mapper) {
        return safeMapping(list, mapper, Collectors.toList());
    }

    public static <T, R, C> C safeMapping(Collection<T> list, Function<T, Optional<R>> mapper, Collector<R, ?, C> collector) {
        return list.stream()
                .map(mapper)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(collector);
    }

    public static <T, R> Function<T, Optional<R>> catchMe(ThrowingFunction<T, R, ?> throwingFunction) {
        return t -> {
            try {
                return Optional.of(throwingFunction.apply(t));
            } catch (Throwable exception) {
                return Optional.empty();
            }
        };
    }

    public static <T, E extends Throwable> Consumer<T> silence(ThrowingConsumer<T, E> consumer) {
        return silence(new ConsumerFunctionAdapter<>(consumer))::apply;
    }

    public static <T, R, E extends Throwable> Function<T, R> silence(ThrowingFunction<T, R, E> consumer) {
        return t -> {
            try {
                return consumer.apply(t);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static class ConsumerFunctionAdapter<T, R, E extends Throwable> implements ThrowingFunction<T, R, E> {
        private final ThrowingConsumer<T, E> wrappedFunction;

        public ConsumerFunctionAdapter(ThrowingConsumer<T, E> functionToWrap) {
            this.wrappedFunction = functionToWrap;
        }

        @Override
        public R apply(T t) throws E {
            wrappedFunction.accept(t);
            return null;
        }
    }

    public static <T> TryFinally<T> process(GarbageThrowingConsumer<T, ?> consumer) {
        return new TryFinally<>(consumer);
    }

    public static class TryFinally<T> {
        private GarbageThrowingConsumer<T,?> throwingConsumer;
        private T t;
        private Garbage garbage;

        public TryFinally(GarbageThrowingConsumer<T, ?> throwingConsumer) {
            this.throwingConsumer = throwingConsumer;
            this.garbage = new Garbage();
        }

        public TryFinally<T> using(T t) {
            this.t = t;
            return this;
        }

        public void cleanup() {
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

    @FunctionalInterface
    public interface ThrowingConsumer<T, E extends Throwable> {
        void accept(T t) throws E;
    }
    @FunctionalInterface
    public interface ThrowingFunction<T, R, E extends Throwable> {
        R apply(T t) throws E;
    }
    @FunctionalInterface
    public interface ThrowingStatement<E extends Throwable> {
        void apply() throws E;
    }
    @FunctionalInterface
    public interface Statement {
        void apply();
    }
}
