package throwinglambdas.functional;

@FunctionalInterface
public interface ThrowingStatement<E extends Throwable> {
    void apply() throws E;
}
