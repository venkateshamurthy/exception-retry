package io.github.venkateshamurthy.exceptional;

/**
 * CheckedBiConsumer is an interface to model accepting two parameters but may throw
 * @param <T>  Ist type of input
 * @param <T2> IInd type of input
 */
@FunctionalInterface
public interface CheckedBiConsumer<T, T2> {
    /**
     * A bi consumer accept method that can throw {@link Throwable}
     * @param t   Ist type input
     * @param t2  IInd type input
     * @throws Throwable can be thrown
     */
    void accept(T t, T2 t2) throws Throwable;
}