package io.github.venkateshamurthy.exceptional;

/**
 * CheckedTriFunction is a checked variant of {@link org.apache.commons.lang3.function.TriFunction}
 * @param <T> type 1
 * @param <T2> type 2
 * @param <T3> type 3
 * @param <R> result type
 */
public interface CheckedTriFunction<T, T2, T3, R> {
    /**
     * Applies the parameters to the function implemented or throws an exception
     * @param t parameter 1
     * @param t2 parameter 2
     * @param t3 parameter 3
     * @return the result in normal circumstance
     * @throws Exception is otherwise thrown
     */
    R apply(T t, T2 t2, T3 t3) throws Exception;
}
