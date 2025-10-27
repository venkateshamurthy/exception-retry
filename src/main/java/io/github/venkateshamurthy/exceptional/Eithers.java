package io.github.venkateshamurthy.exceptional;

import io.github.resilience4j.core.functions.*;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.SneakyThrows;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;

import static io.github.venkateshamurthy.exceptional.RxRunnable.toCheckedRunnable;
import static io.github.venkateshamurthy.exceptional.RxRunnable.toRunnable;
import static io.github.venkateshamurthy.exceptional.RxTry.ceMapper;

/**
 * Eithers is a convenient utility to convert different forms of executors to [@Either}
 */
@ExtensionMethod({RxFunction.class, RxSupplier.class, RxTry.class, RxConsumer.class, RxRunnable.class})
@UtilityClass  @SuppressWarnings("java:S1118")
public class Eithers {
    /**
     * An {@link Either either's} value or exception getter without any translations
     * @param either the input Either
     * @return result
     * @param <R> type of result
     */
    @SneakyThrows
    public static <R> R orElseThrow(Either<Exception, R> either) {
        return either.getOrElseThrow(Function.identity());
    }
    /**
     * Try to Either transform
     *
     * @param trier  the Try instance
     * @param mapper exception transform
     * @param <V>    the return type
     * @return Either
     */
    public static <V> Either<Exception, V> tryToEither(Try<V> trier, Map<Class<? extends Exception>, Supplier<Exception>> mapper) {
        return trier.mapExceptions(mapper).toEither().mapLeft(ceMapper);
    }

    /**
     * Try to Either transform
     *
     * @param trier             the Try instance
     * @param exceptionSupplier exception supplier
     * @param <V>               the return type
     * @return Either
     */
    public static <V> Either<Exception, V> tryToEither(Try<V> trier, Supplier<Exception> exceptionSupplier) {
        return trier.mapException(Exception.class, exceptionSupplier)
                    .toEither().mapLeft(ceMapper);
    }

    /**
     * tryToEither transform using a {@link UnaryOperator exception transformer}
     * @param trier the input {@code Try}
     * @param exceptionMapper is the functional transform of exception
     * @return Either
     * @param <V> is the type of result
     */
    public static <V> Either<Exception, V> tryToEither(Try<V> trier, UnaryOperator<Exception> exceptionMapper) {
        return trier.mapException(Exception.class, exceptionMapper)
                    .toEither().mapLeft(ceMapper);
    }

    /**
     * tryToEither transform using a {@link UnaryOperator exception transformer}
     * @param trier the input {@code Try}
     * @return Either
     * @param <V> is the type of result
     */
    public static <V> Either<Exception, V> tryToEither(Try<V> trier) {
        return tryToEither(trier, UnaryOperator.identity());
    }

    /**
     * Creates an Either with passed in consumer and arguments and exception mapper
     *
     * @param consumer to execute
     * @param u        passed in argument
     * @param mapper   to transform exception
     * @param <U>      type of input
     * @return Either
     */
    public static <U> Either<Exception, Void> either(Consumer<U> consumer, U u,
                                                     Map<Class<? extends Exception>, Supplier<Exception>> mapper) {
        return tryToEither(toRunnable(() -> consumer.accept(u)).tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in consumer and arguments and exception mapper
     *
     * @param biConsumer to execute
     * @param u          passed in argument
     * @param v          passed in argument
     * @param mapper     to transform exception
     * @param <U>        type of input
     * @param <V>        type of input
     * @return Either
     */
    public static <U, V> Either<Exception, Void> either(BiConsumer<U, V> biConsumer, U u, V v,
                                                        Map<Class<? extends Exception>, Supplier<Exception>> mapper) {
        return tryToEither(toRunnable(() -> biConsumer.accept(u, v)).tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in consumer and arguments and exception mapper
     *
     * @param checkedConsumer to execute
     * @param u               is the input argument
     * @param mapper          to transform exception
     * @param <U>             is type of input
     * @return Either
     */
    public static <U> Either<Exception, Void> either(CheckedConsumer<U> checkedConsumer, U u,
                                                     Map<Class<? extends Exception>, Supplier<Exception>> mapper) {
        return tryToEither(toCheckedRunnable(() -> checkedConsumer.accept(u)).tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in consumer and arguments and exception mapper
     *
     * @param checkedBiConsumer to execute
     * @param u                 passed in argument
     * @param v                 passed in argument
     * @param mapper            to transform exception
     * @param <U>               type of input
     * @param <V>               type of input
     * @return Either
     */
    public static <U, V> Either<Exception, Void> either(CheckedBiConsumer<U, V> checkedBiConsumer, U u, V v,
                                                        Map<Class<? extends Exception>, Supplier<Exception>> mapper) {
        return tryToEither(toCheckedRunnable(() -> checkedBiConsumer.accept(u, v)).tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in supplier and arguments and exception mapper
     *
     * @param supplier to execute
     * @param mapper   to transform exception
     * @param <V>      is type of output
     * @return Either
     */
    public static <V> Either<Exception, V> either(Supplier<V> supplier,
                                                  Map<Class<? extends Exception>, Supplier<Exception>> mapper) {
        return tryToEither(supplier.tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in supplier and arguments and exception mapper
     *
     * @param checkedSupplier to execute
     * @param mapper          to transform exception
     * @param <V>             is type of output
     * @return Either
     */
    public static <V> Either<Exception, V> either(CheckedSupplier<V> checkedSupplier,
                                                  Map<Class<? extends Exception>, Supplier<Exception>> mapper) {
        return tryToEither(checkedSupplier.tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in callable and arguments and exception mapper
     *
     * @param callable to execute
     * @param mapper   to transform exception
     * @param <V>      is type of output
     * @return Either
     */
    public static <V> Either<Exception, V> either(Callable<V> callable,
                                                  Map<Class<? extends Exception>, Supplier<Exception>> mapper) {
        return tryToEither(callable.tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in runnable and arguments and exception mapper
     *
     * @param runnable to execute
     * @param mapper   to transform exception
     * @return Either
     */
    public static Either<Exception, Void> either(Runnable runnable,
                                                 Map<Class<? extends Exception>, Supplier<Exception>> mapper) {
        return tryToEither(runnable.tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in runnable and arguments and exception mapper
     *
     * @param runnable to execute
     * @param mapper   to transform exception
     * @return Either
     */
    public static Either<Exception, Void> either(CheckedRunnable runnable,
                                                 Map<Class<? extends Exception>, Supplier<Exception>> mapper) {
        return tryToEither(runnable.tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in function and arguments and exception mapper
     *
     * @param function to execute
     * @param u        passed in argument
     * @param mapper   to transform exception
     * @param <U>      type of input
     * @param <V>      type of Output
     * @return Either
     */
    public static <U, V> Either<Exception, V> either(Function<U, V> function, U u,
                                                     Map<Class<? extends Exception>, Supplier<Exception>> mapper) {
        return tryToEither(function.tryWrap(u), mapper);
    }

    /**
     * Creates an Either with passed in function and arguments and exception mapper
     *
     * @param checkedFunction to execute
     * @param u               passed in argument
     * @param mapper          to transform exception
     * @param <U>             type of input
     * @param <V>             type of Output
     * @return Either
     */
    public static <U, V> Either<Exception, V> either(CheckedFunction<U, V> checkedFunction, U u,
                                                     Map<Class<? extends Exception>, Supplier<Exception>> mapper) {
        return tryToEither(checkedFunction.tryWrap(u), mapper);
    }

    /**
     * Creates an Either with passed in function and arguments and exception mapper
     *
     * @param biFunction to execute
     * @param t          1st argument
     * @param u          2nd argument
     * @param mapper     to transform exception
     * @param <T>        1st type of input
     * @param <U>        2nd type of input
     * @param <V>        type of Output
     * @return Either
     */
    public static <T, U, V> Either<Exception, V> either(BiFunction<T, U, V> biFunction, T t, U u,
                                                        Map<Class<? extends Exception>, Supplier<Exception>> mapper) {
        return tryToEither(biFunction.tryWrap(t, u), mapper);
    }

    /**
     * Creates an Either with passed in function and arguments and exception mapper
     *
     * @param checkedBiFunction to execute
     * @param t                 passed in argument
     * @param u                 passed in argument
     * @param mapper            to transform exception
     * @param <T>               type of input
     * @param <U>               type of input
     * @param <V>               type of Output
     * @return Either
     */
    public static <T, U, V> Either<Exception, V> either(CheckedBiFunction<T, U, V> checkedBiFunction, T t, U u,
                                                        Map<Class<? extends Exception>, Supplier<Exception>> mapper) {
        return tryToEither(checkedBiFunction.tryWrap(t, u), mapper);
    }

    //------------------------ Supplier variant --------------------------------

    /**
     * Creates an Either with passed in consumer and arguments and exception mapper
     *
     * @param consumer to execute
     * @param u        passed in argument
     * @param mapper   to transform exception
     * @param <U>      type of input
     * @return Either
     */
    public static <U> Either<Exception, Void> either(Consumer<U> consumer, U u,
                                                     Supplier<Exception> mapper) {
        return tryToEither(toRunnable(() -> consumer.accept(u)).tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in consumer and arguments and exception mapper
     *
     * @param consumer to execute
     * @param u        passed in argument
     * @param mapper   to transform exception
     * @param <U>      type of input
     * @return Either
     */
    public static <U> Either<Exception, Void> either(Consumer<U> consumer, U u,
                                                     UnaryOperator<Exception> mapper) {
        return tryToEither(toRunnable(() -> consumer.accept(u)).tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in consumer and arguments
     *
     * @param consumer to execute
     * @param u        passed in argument
     * @param <U>      type of input
     * @return Either
     */
    public static <U> Either<Exception, Void> either(Consumer<U> consumer, U u) {
        return tryToEither(toRunnable(() -> consumer.accept(u)).tryWrap());
    }

    /**
     * Creates an Either with passed in consumer and arguments and exception mapper
     *
     * @param biConsumer to execute
     * @param u          passed in argument
     * @param v          passed in argument
     * @param mapper     to transform exception
     * @param <U>        type of input
     * @param <V>        type of input
     * @return Either
     */
    public static <U, V> Either<Exception, Void> either(BiConsumer<U, V> biConsumer, U u, V v,
                                                        Supplier<Exception> mapper) {
        return tryToEither(toRunnable(() -> biConsumer.accept(u, v)).tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in consumer and arguments and exception mapper
     *
     * @param biConsumer to execute
     * @param u          passed in argument
     * @param v          passed in argument
     * @param mapper     to transform exception
     * @param <U>        type of input
     * @param <V>        type of input
     * @return Either
     */
    public static <U, V> Either<Exception, Void> either(BiConsumer<U, V> biConsumer, U u, V v,
                                                        UnaryOperator<Exception> mapper) {
        return tryToEither(toRunnable(() -> biConsumer.accept(u, v)).tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in consumer and arguments
     *
     * @param biConsumer to execute
     * @param u          passed in argument
     * @param v          passed in argument
     * @param <U>        type of input
     * @param <V>        type of input
     * @return Either
     */
    public static <U, V> Either<Exception, Void> either(BiConsumer<U, V> biConsumer, U u, V v) {
        return tryToEither(toRunnable(() -> biConsumer.accept(u, v)).tryWrap());
    }

    /**
     * Creates an Either with passed in consumer and arguments and exception mapper
     *
     * @param checkedConsumer to execute
     * @param u               input argument
     * @param mapper          to transform exception
     * @param <U>             is type of input
     * @return Either
     */
    public static <U> Either<Exception, Void> either(CheckedConsumer<U> checkedConsumer, U u,
                                                     Supplier<Exception> mapper) {
        return tryToEither(toCheckedRunnable(() -> checkedConsumer.accept(u)).tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in consumer and arguments and exception mapper
     *
     * @param checkedConsumer to execute
     * @param u               input argument
     * @param mapper          to transform exception
     * @param <U>             is type of input
     * @return Either
     */
    public static <U> Either<Exception, Void> either(CheckedConsumer<U> checkedConsumer, U u,
                                                     UnaryOperator<Exception> mapper) {
        return tryToEither(toCheckedRunnable(() -> checkedConsumer.accept(u)).tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in consumer and arguments
     *
     * @param checkedConsumer to execute
     * @param u               input argument
     * @param <U>             is type of input
     * @return Either
     */
    public static <U> Either<Exception, Void> either(CheckedConsumer<U> checkedConsumer, U u) {
        return tryToEither(toCheckedRunnable(() -> checkedConsumer.accept(u)).tryWrap());
    }

    /**
     * Creates an Either with passed in consumer and arguments and exception mapper
     *
     * @param checkedBiConsumer to execute
     * @param u                 passed in argument
     * @param v                 passed in argument
     * @param mapper            to transform exception
     * @param <U>               type of input
     * @param <V>               type of input
     * @return Either
     */
    public static <U, V> Either<Exception, Void> either(CheckedBiConsumer<U, V> checkedBiConsumer, U u, V v,
                                                        Supplier<Exception> mapper) {
        return tryToEither(toCheckedRunnable(() -> checkedBiConsumer.accept(u, v)).tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in consumer and arguments and exception mapper
     *
     * @param checkedBiConsumer to execute
     * @param u                 passed in argument
     * @param v                 passed in argument
     * @param mapper            to transform exception
     * @param <U>               type of input
     * @param <V>               type of input
     * @return Either
     */
    public static <U, V> Either<Exception, Void> either(CheckedBiConsumer<U, V> checkedBiConsumer, U u, V v,
                                                        UnaryOperator<Exception> mapper) {
        return tryToEither(toCheckedRunnable(() -> checkedBiConsumer.accept(u, v)).tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in consumer and arguments and exception mapper
     *
     * @param checkedBiConsumer to execute
     * @param u                 passed in argument
     * @param v                 passed in argument
     * @param <U>               type of input
     * @param <V>               type of input
     * @return Either
     */
    public static <U, V> Either<Exception, Void> either(CheckedBiConsumer<U, V> checkedBiConsumer, U u, V v) {
        return tryToEither(toCheckedRunnable(() -> checkedBiConsumer.accept(u, v)).tryWrap());
    }

    /**
     * Creates an Either with passed in supplier and exception mapper
     *
     * @param supplier to execute
     * @param mapper   to transform exception
     * @param <V>      is type of output
     * @return Either
     */
    public static <V> Either<Exception, V> either(Supplier<V> supplier,
                                                  Supplier<Exception> mapper) {
        return tryToEither(supplier.tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in supplier and exception mapper
     *
     * @param supplier to execute
     * @param mapper   to transform exception
     * @param <V>      is type of output
     * @return Either
     */
    public static <V> Either<Exception, V> either(Supplier<V> supplier,
                                                  UnaryOperator<Exception> mapper) {
        return tryToEither(supplier.tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in supplier
     *
     * @param supplier to execute
     * @param <V>      is type of output
     * @return Either
     */
    public static <V> Either<Exception, V> either(Supplier<V> supplier) {
        return tryToEither(supplier.tryWrap());
    }


    /**
     * Creates an Either with passed in supplier and exception mapper
     *
     * @param checkedSupplier to execute
     * @param mapper          to transform exception
     * @param <V>             is type of output
     * @return Either
     */
    public static <V> Either<Exception, V> either(CheckedSupplier<V> checkedSupplier,
                                                  Supplier<Exception> mapper) {
        return tryToEither(checkedSupplier.tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in supplier and exception mapper
     *
     * @param checkedSupplier to execute
     * @param mapper          to transform exception
     * @param <V>             is type of output
     * @return Either
     */
    public static <V> Either<Exception, V> either(CheckedSupplier<V> checkedSupplier,
                                                  UnaryOperator<Exception> mapper) {
        return tryToEither(checkedSupplier.tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in supplier
     *
     * @param checkedSupplier to execute
     * @param <V>             is type of output
     * @return Either
     */
    public static <V> Either<Exception, V> either(CheckedSupplier<V> checkedSupplier) {
        return tryToEither(checkedSupplier.tryWrap());
    }

    /**
     * Creates an Either with passed in callable and exception mapper
     *
     * @param callable to execute
     * @param mapper   to transform exception
     * @param <V>      is type of output
     * @return Either
     */
    public static <V> Either<Exception, V> either(Callable<V> callable,
                                                  Supplier<Exception> mapper) {
        return tryToEither(callable.tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in callable and exception mapper
     *
     * @param callable to execute
     * @param mapper   to transform exception
     * @param <V>      is type of output
     * @return Either
     */
    public static <V> Either<Exception, V> either(Callable<V> callable,
                                                  UnaryOperator<Exception> mapper) {
        return tryToEither(callable.tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in callable
     *
     * @param callable to execute
     * @param <V>      is type of output
     * @return Either
     */
    public static <V> Either<Exception, V> either(Callable<V> callable) {
        return tryToEither(callable.tryWrap());
    }

    /**
     * Creates an Either with passed in runnable and exception mapper
     *
     * @param runnable to execute
     * @param mapper   to transform exception
     * @return Either
     */
    public static Either<Exception, Void> either(Runnable runnable,
                                                 Supplier<Exception> mapper) {
        return tryToEither(runnable.tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in runnable and exception mapper
     *
     * @param runnable to execute
     * @param mapper   to transform exception
     * @return Either
     */
    public static Either<Exception, Void> either(Runnable runnable,
                                                 UnaryOperator<Exception> mapper) {
        return tryToEither(runnable.tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in runnable
     *
     * @param runnable to execute
     * @return Either
     */
    public static Either<Exception, Void> either(Runnable runnable) {
        return tryToEither(runnable.tryWrap());
    }

    /**
     * Creates an Either with passed in function and arguments and exception mapper
     *
     * @param function to execute
     * @param u        passed in argument
     * @param mapper   to transform exception
     * @param <U>      type of input
     * @param <V>      type of Output
     * @return Either
     */
    public static <U, V> Either<Exception, V> either(Function<U, V> function, U u,
                                                     Supplier<Exception> mapper) {
        return tryToEither(function.tryWrap(u), mapper);
    }

    /**
     * Creates an Either with passed in function and arguments and exception mapper
     *
     * @param function to execute
     * @param u        passed in argument
     * @param mapper   to transform exception
     * @param <U>      type of input
     * @param <V>      type of Output
     * @return Either
     */
    public static <U, V> Either<Exception, V> either(Function<U, V> function, U u,
                                                     UnaryOperator<Exception> mapper) {
        return tryToEither(function.tryWrap(u), mapper);
    }

    /**
     * Creates an Either with passed in function and arguments
     *
     * @param function to execute
     * @param u        passed in argument
     * @param <U>      type of input
     * @param <V>      type of Output
     * @return Either
     */
    public static <U, V> Either<Exception, V> either(Function<U, V> function, U u) {
        return tryToEither(function.tryWrap(u));
    }

    /**
     * Creates an Either with passed in function and arguments and exception mapper
     *
     * @param checkedFunction to execute
     * @param u               passed in argument
     * @param mapper          to transform exception
     * @param <U>             type of input
     * @param <V>             type of Output
     * @return Either
     */
    public static <U, V> Either<Exception, V> either(CheckedFunction<U, V> checkedFunction, U u,
                                                     Supplier<Exception> mapper) {
        return tryToEither(checkedFunction.tryWrap(u), mapper);
    }

    /**
     * Creates an Either with passed in function and arguments and exception mapper
     *
     * @param checkedFunction to execute
     * @param u               passed in argument
     * @param mapper          to transform exception
     * @param <U>             type of input
     * @param <V>             type of Output
     * @return Either
     */
    public static <U, V> Either<Exception, V> either(CheckedFunction<U, V> checkedFunction, U u,
                                                     UnaryOperator<Exception> mapper) {
        return tryToEither(checkedFunction.tryWrap(u), mapper);
    }

    /**
     * Creates an Either with passed in function and arguments
     *
     * @param checkedFunction to execute
     * @param u               passed in argument
     * @param <U>             type of input
     * @param <V>             type of Output
     * @return Either
     */
    public static <U, V> Either<Exception, V> either(CheckedFunction<U, V> checkedFunction, U u) {
        return tryToEither(checkedFunction.tryWrap(u));
    }

    /**
     * Creates an Either with passed in function and arguments and exception mapper
     *
     * @param biFunction to execute
     * @param t          1st argument
     * @param u          2nd argument
     * @param mapper     to transform exception
     * @param <T>        1st type of input
     * @param <U>        2nd type of input
     * @param <V>        type of Output
     * @return Either
     */
    public static <T, U, V> Either<Exception, V> either(BiFunction<T, U, V> biFunction, T t, U u,
                                                        Supplier<Exception> mapper) {
        return tryToEither(biFunction.tryWrap(t, u), mapper);
    }

    /**
     * Creates an Either with passed in function and arguments and exception mapper
     *
     * @param biFunction to execute
     * @param t          1st  argument
     * @param u          2nd argument
     * @param mapper     to transform exception
     * @param <T>        1st type of input
     * @param <U>        2nd type of input
     * @param <V>        type of Output
     * @return Either
     */
    public static <T, U, V> Either<Exception, V> either(BiFunction<T, U, V> biFunction, T t, U u,
                                                        UnaryOperator<Exception> mapper) {
        return tryToEither(biFunction.tryWrap(t, u), mapper);
    }

    /**
     * Creates an Either with passed in function and arguments
     *
     * @param biFunction to execute
     * @param t          1st argument
     * @param u          2nd argument
     * @param <T>        1st type of input
     * @param <U>        2nd type of input
     * @param <V>        type of Output
     * @return Either
     */
    public static <T, U, V> Either<Exception, V> either(BiFunction<T, U, V> biFunction, T t, U u) {
        return tryToEither(biFunction.tryWrap(t, u));
    }

    /**
     * Creates an Either with passed in function and arguments and exception mapper
     *
     * @param checkedBiFunction to execute
     * @param t                 passed in argument
     * @param u                 passed in argument
     * @param mapper            to transform exception
     * @param <T>               type of input
     * @param <U>               type of input
     * @param <V>               type of Output
     * @return Either
     */
    public static <T, U, V> Either<Exception, V> either(CheckedBiFunction<T, U, V> checkedBiFunction, T t, U u,
                                                        Supplier<Exception> mapper) {
        return tryToEither(checkedBiFunction.tryWrap(t, u), mapper);
    }

    /**
     * Creates an Either with passed in function and arguments and exception mapper
     *
     * @param checkedBiFunction to execute
     * @param t                 passed in argument
     * @param u                 passed in argument
     * @param mapper            to transform exception
     * @param <T>               type of input
     * @param <U>               type of input
     * @param <V>               type of Output
     * @return Either
     */
    public static <T, U, V> Either<Exception, V> either(CheckedBiFunction<T, U, V> checkedBiFunction, T t, U u,
                                                        UnaryOperator<Exception> mapper) {
        return tryToEither(checkedBiFunction.tryWrap(t, u), mapper);
    }

    /**
     * Creates an Either with passed in function and arguments
     *
     * @param checkedBiFunction to execute
     * @param t                 passed in argument
     * @param u                 passed in argument
     * @param <T>               type of input
     * @param <U>               type of input
     * @param <V>               type of Output
     * @return Either
     */
    public static <T, U, V> Either<Exception, V> either(CheckedBiFunction<T, U, V> checkedBiFunction, T t, U u) {
        return tryToEither(checkedBiFunction.tryWrap(t, u));
    }

    /**
     * Creates an Either with passed in runnable  and exception mapper
     *
     * @param runnable to execute
     * @param mapper   to transform exception
     * @return Either
     */
    public static Either<Exception, Void> either(CheckedRunnable runnable,
                                                 Supplier<Exception> mapper) {
        return tryToEither(runnable.tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in runnable  and exception mapper
     *
     * @param runnable to execute
     * @param mapper   to transform exception
     * @return Either
     */
    public static Either<Exception, Void> either(CheckedRunnable runnable,
                                                 UnaryOperator<Exception> mapper) {
        return tryToEither(runnable.tryWrap(), mapper);
    }

    /**
     * Creates an Either with passed in runnable
     *
     * @param runnable to execute
     * @return Either
     */
    public static Either<Exception, Void> either(CheckedRunnable runnable) {
        return tryToEither(runnable.tryWrap());
    }

    /**
     * Either transform of a {@link TriFunction} that accepts 3 arguments with exception mapper
     * @param triFunction a {@code TriFunction}
     * @param t parameter 1
     * @param u parameter 2
     * @param v parameter 3
     * @param mapper to transform exception
     * @return Either
     * @param <T> type 1
     * @param <U> type 2
     * @param <V> type 3
     * @param <R> result type
     */
    public static <T, U, V, R> Either<Exception, R> either(TriFunction<T, U, V, R> triFunction, T t, U u, V v,
                                                            Map<Class<? extends Exception>, Supplier<Exception>> mapper) {
        return tryToEither(triFunction.tryWrap(t, u, v), mapper);
    }

    /**
     * Either transform of a {@link TriFunction} that accepts 3 arguments with exception mapper
     * @param triFunction a {@code TriFunction}
     * @param t parameter 1
     * @param u parameter 2
     * @param v parameter 3
     * @param mapper to transform exception
     * @return Either
     * @param <T> type 1
     * @param <U> type 2
     * @param <V> type 3
     * @param <R> result type
     */
    public static <T, U, V, R> Either<Exception, R> either(TriFunction<T, U, V, R> triFunction, T t, U u, V v,
                                                           Supplier<Exception> mapper) {
        return tryToEither(triFunction.tryWrap(t, u, v), mapper);
    }

    /**
     * Either transform of a {@link TriFunction} that accepts 3 arguments with exception mapper
     * @param triFunction a {@code TriFunction}
     * @param t parameter 1
     * @param u parameter 2
     * @param v parameter 3
     * @param mapper to transform exception
     * @return Either
     * @param <T> type 1
     * @param <U> type 2
     * @param <V> type 3
     * @param <R> result type
     */
    public static <T, U, V, R> Either<Exception, R> either(TriFunction<T, U, V, R> triFunction, T t, U u, V v,
                                                           UnaryOperator<Exception> mapper) {
        return tryToEither(triFunction.tryWrap(t, u, v), mapper);
    }

    /**
     * Either transform of a {@link TriFunction} that accepts 3 arguments
     * @param triFunction a {@code TriFunction}
     * @param t parameter 1
     * @param u parameter 2
     * @param v parameter 3
     * @return Either
     * @param <T> type 1
     * @param <U> type 2
     * @param <V> type 3
     * @param <R> result type
     */
    public static <T, U, V, R> Either<Exception, R> either(TriFunction<T, U, V, R> triFunction, T t, U u, V v) {
        return tryToEither(triFunction.tryWrap(t, u, v));
    }

    /**
     * Either transform of a {@link CheckedTriFunction} that accepts 3 arguments with exception mapper
     * @param triFunction a {@code CheckedTriFunction}
     * @param t parameter 1
     * @param u parameter 2
     * @param v parameter 3
     * @param mapper to transform exception
     * @return Either
     * @param <T> type 1
     * @param <U> type 2
     * @param <V> type 3
     * @param <R> result type
     */
    public static <T, U, V, R> Either<Exception, R> either(CheckedTriFunction<T, U, V, R> triFunction, T t, U u, V v,
                                                           Map<Class<? extends Exception>, Supplier<Exception>> mapper) {
        return tryToEither(triFunction.tryWrap(t, u, v), mapper);
    }

    /**
     * Either transform of a {@link CheckedTriFunction} that accepts 3 arguments with exception mapper
     * @param triFunction a {@code CheckedTriFunction}
     * @param t parameter 1
     * @param u parameter 2
     * @param v parameter 3
     * @param mapper to transform exception
     * @return Either
     * @param <T> type 1
     * @param <U> type 2
     * @param <V> type 3
     * @param <R> result type
     */
    public static <T, U, V, R> Either<Exception, R> either(CheckedTriFunction<T, U, V, R> triFunction, T t, U u, V v,
                                                           Supplier<Exception> mapper) {
        return tryToEither(triFunction.tryWrap(t, u, v), mapper);
    }

    /**
     * Either transform of a {@link CheckedTriFunction} that accepts 3 arguments with exception mapper
     * @param triFunction a {@code CheckedTriFunction}
     * @param t parameter 1
     * @param u parameter 2
     * @param v parameter 3
     * @param mapper to transform exception
     * @return Either
     * @param <T> type 1
     * @param <U> type 2
     * @param <V> type 3
     * @param <R> result type
     */
    public static <T, U, V, R> Either<Exception, R> either(CheckedTriFunction<T, U, V, R> triFunction, T t, U u, V v,
                                                           UnaryOperator<Exception> mapper) {
        return tryToEither(triFunction.tryWrap(t, u, v), mapper);
    }

    /**
     * Either transform of a {@link CheckedTriFunction} that accepts 3 arguments
     * @param triFunction a {@code CheckedTriFunction}
     * @param t parameter 1
     * @param u parameter 2
     * @param v parameter 3
     * @return Either
     * @param <T> type 1
     * @param <U> type 2
     * @param <V> type 3
     * @param <R> result type
     */
    public static <T, U, V, R> Either<Exception, R> either(CheckedTriFunction<T, U, V, R> triFunction, T t, U u, V v) {
        return tryToEither(triFunction.tryWrap(t, u, v));
    }
}