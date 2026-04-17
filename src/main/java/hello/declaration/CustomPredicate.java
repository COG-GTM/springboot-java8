package hello.declaration;

/**
 * Use as a Functional Interface
 * @param <T>
 */
@FunctionalInterface
public interface CustomPredicate<T> {
    boolean test(T t);
}
