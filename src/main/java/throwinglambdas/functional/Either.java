package throwinglambdas.functional;

import java.util.Objects;
import java.util.Optional;

public class Either<L, R> {

    private Optional<L> l;
    private Optional<R> r;

    private Either() { /* Should never be used. */ }
    private Either(Optional<L> l, Optional<R> r) {
        this.l = l;
        this.r = r;
    }

    public static <L, R> Either<L, R> left(L l) {
        Objects.requireNonNull(l);
        return new Either<>(
                Optional.of(l),
                Optional.empty()
        );
    }

    public static <L, R> Either<L, R> right(R r) {
        Objects.requireNonNull(r);
        return new Either<>(
                Optional.empty(),
                Optional.of(r)
        );
    }

    public boolean isLeft() {
        return this.l.isPresent();
    }

    public boolean isRight() {
        return this.r.isPresent();
    }

    public L getLeft() {
        if (this.l.isPresent()) {
            return l.get();
        }
        throw new IllegalStateException("Either is of type right.");
    }

    public R getRight() {
        if (this.r.isPresent()) {
            return r.get();
        }
        throw new IllegalStateException("Either is of type left.");
    }
}
