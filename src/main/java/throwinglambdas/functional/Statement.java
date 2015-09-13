package throwinglambdas.functional;

/**
 * A statement that can be executed. It does not take any argument and does not return anything.
 */
@FunctionalInterface
public interface Statement {

    /**
     * Execute the method.
     */
    void apply();
}
