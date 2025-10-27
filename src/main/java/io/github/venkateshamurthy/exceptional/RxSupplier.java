package io.github.venkateshamurthy.exceptional;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.functions.CheckedBiFunction;
import io.github.resilience4j.core.functions.CheckedConsumer;
import io.github.resilience4j.core.functions.CheckedFunction;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import lombok.SneakyThrows;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.*;

import static io.github.venkateshamurthy.exceptional.RxTry.ceMapper;
import static io.github.venkateshamurthy.exceptional.RxTry.rteMapper;

/**
 * A convenient wrapper to {@link RxSupplier}
 */
@UtilityClass  @SuppressWarnings("java:S1118")
@ExtensionMethod(RxTry.class)
public class RxSupplier {
    /**
     * A reflexive wrapper to {@link CheckedSupplier}
     *
     * @param supplier input
     * @param <R>      type of result
     * @return as is the input
     */
    public static <R> CheckedSupplier<R> toCheckedSupplier(CheckedSupplier<R> supplier) {
        return supplier;
    }

    /**
     * A reflexive wrapper to {@link Supplier}
     *
     * @param supplier input
     * @param <R>      type of result
     * @return as is the input
     */
    public static <R> Supplier<R> toSupplier(Supplier<R> supplier) {
        return supplier;
    }

    /**
     * Gets a Supplier modeling the {@link TriFunction}
     * @param triFunction to operate on three arguments
     * @param t parameter 1
     * @param t2 parameter 2
     * @param t3 parameter 3
     * @return Supplier wrappering the triFunction execution
     * @param <T> type 1
     * @param <T2> type 2
     * @param <T3> type 3
     * @param <R> result type
     */
    public static <T, T2, T3, R> Supplier<R> toSupplier(TriFunction<T,T2,T3,R> triFunction, T t, T2 t2, T3 t3) {
        return ()->triFunction.apply(t, t2, t3);
    }

    /**
     * A {@link Retry} wrapper to {@link Supplier}
     *
     * @param supplier input
     * @param retry    a retryer
     * @param <R>      type of result
     * @return retry wrapped input
     */
    public static <R> Supplier<R> retrySupplier(Supplier<R> supplier, Retry retry) {
        return Retry.decorateSupplier(retry, supplier);
    }

    /**
     * A {@link RateLimiter} wrapper to {@link Supplier}
     *
     * @param supplier    input
     * @param rateLimiter a rate limiter
     * @param <R>         type of result
     * @return rate limited wrapped input
     */
    public static <R> Supplier<R> rateLimitSupplier(Supplier<R> supplier, RateLimiter rateLimiter) {
        return RateLimiter.decorateSupplier(rateLimiter, supplier);
    }

    /**
     * A {@link CircuitBreaker} wrapper to {@link Supplier}
     *
     * @param supplier       input
     * @param circuitBreaker a circuit breaker
     * @param <R>            type of result
     * @return circuit breaker wrapped input
     */
    public static <R> Supplier<R> circuitBreakSupplier(Supplier<R> supplier, CircuitBreaker circuitBreaker) {
        return CircuitBreaker.decorateSupplier(circuitBreaker, supplier);
    }

    /**
     * A {@link Bulkhead} wrapper to {@link Supplier}
     *
     * @param supplier input
     * @param <R>      type of result
     * @param bulkhead a bulk head
     * @return bulkhead wrapped input
     */
    public static <R> Supplier<R> bulkheadSupplier(Supplier<R> supplier, Bulkhead bulkhead) {
        return Bulkhead.decorateSupplier(bulkhead, supplier);
    }

    /**
     * A {@link Retry} wrapper to {@link CheckedSupplier}
     *
     * @param supplier input
     * @param retry    a retry
     * @param <R>      type of result
     * @return retry wrapped input
     */
    public static <R> CheckedSupplier<R> retryCheckedSupplier(CheckedSupplier<R> supplier, Retry retry) {
        return Retry.decorateCheckedSupplier(retry, supplier);
    }

    /**
     * A {@link RateLimiter} wrapper to {@link CheckedSupplier}
     *
     * @param supplier    input
     * @param rateLimiter rate limiter
     * @param <R>         type of result
     * @return rate limited wrapped input
     */
    public static <R> CheckedSupplier<R> rateLimitCheckedSupplier(CheckedSupplier<R> supplier, RateLimiter rateLimiter) {
        return RateLimiter.decorateCheckedSupplier(rateLimiter, supplier);
    }

    /**
     * A {@link CircuitBreaker} wrapper to {@link CheckedSupplier}
     *
     * @param supplier       input
     * @param circuitBreaker a circuit breaker
     * @param <R>            type of result
     * @return circuit breaker wrapped input
     */
    public static <R> CheckedSupplier<R> circuitBreakCheckedSupplier(CheckedSupplier<R> supplier, CircuitBreaker circuitBreaker) {
        return CircuitBreaker.decorateCheckedSupplier(circuitBreaker, supplier);
    }

    /**
     * A {@link Bulkhead} wrapper to {@link CheckedSupplier}
     *
     * @param supplier input
     * @param bulkhead bulk head
     * @param <R>      type of result
     * @return bulkhead wrapped input
     */
    public static <R> CheckedSupplier<R> bulkheadCheckedSupplier(CheckedSupplier<R> supplier, Bulkhead bulkhead) {
        return Bulkhead.decorateCheckedSupplier(bulkhead, supplier);
    }

    // Convenience transforms from other forms to supplier

    /**
     * A  {@link Supplier} form of {@link Consumer}
     *
     * @param consumer input
     * @param t        the input to the consumer passed
     * @param <T>      type of consumer
     * @return Supplier wrapped consumer
     */
    public static <T> Supplier<Void> toSupplier(Consumer<T> consumer, T t) {
        return () -> {
            consumer.accept(t);
            return null;
        };
    }

    /**
     * A  {@link Supplier} form of {@link BiConsumer}
     *
     * @param biConsumer input
     * @param t          1st input to the consumer passed
     * @param t2         2nd input to the consumer passed
     * @param <T>        type of result
     * @param <T2>       type of result
     * @return Supplier wrapped input
     */
    public static <T, T2> Supplier<Void> toSupplier(BiConsumer<T, T2> biConsumer, T t, T2 t2) {
        return () -> {
            biConsumer.accept(t, t2);
            return null;
        };
    }

    /**
     * A  {@link Supplier} form of {@link BiFunction}
     *
     * @param biFunction input
     * @param t          1st input to the biFunction
     * @param t2         2nd input to the biFunction
     * @param <T>        type of input
     * @param <T2>       type of input
     * @param <R>        type of result
     * @return Supplier wrapped input
     */
    public static <T, T2, R> Supplier<R> toSupplier(BiFunction<T, T2, R> biFunction, T t, T2 t2) {
        return () -> biFunction.apply(t, t2);
    }

    /**
     * A  {@link Supplier} form of {@link Function}
     * @param f   input
     * @param t   1st input to the function
     * @param <T> type of input to function
     * @param <R> type of result
     * @return Supplier wrapped input
     */
    public static <T, R> Supplier<R> toSupplier(Function<T, R> f, T t) {
        return () -> f.apply(t);
    }

    /**
     * A  {@link CheckedSupplier} form of {@link CheckedConsumer}
     *
     * @param consumer input
     * @param t        1st input to the consumer
     * @param <T>      type of input to consumer
     * @return CheckedSupplier wrapped input
     */
    public static <T> CheckedSupplier<Void> toCheckedSupplier(CheckedConsumer<T> consumer, T t) {
        return () -> {
            consumer.accept(t);
            return null;
        };
    }

    /**
     * A  {@link CheckedSupplier} form of {@link CheckedBiConsumer}
     *
     * @param checkedBiConsumer input
     * @param t                 1st input to the checkedBiConsumer
     * @param t2                2nd input to the checkedBiConsumer
     * @param <T>               type of input
     * @param <T2>              type of input
     * @return CheckedSupplier wrapped input
     */
    public static <T, T2> CheckedSupplier<Void> toCheckedSupplier(CheckedBiConsumer<T, T2> checkedBiConsumer, T t, T2 t2) {
        return () -> {
            checkedBiConsumer.accept(t, t2);
            return null;
        };
    }

    /**
     * A  {@link CheckedSupplier} form of {@link CheckedBiFunction}
     *
     * @param checkedBiFunction input
     * @param t                 1st input to the checkedBiFunction
     * @param t2                2nd input to the checkedBiFunction
     * @param <T>               type of input
     * @param <T2>              type of input
     * @param <R>               type of result
     * @return CheckedSupplier wrapped input
     */
    public static <T, T2, R> CheckedSupplier<R> toCheckedSupplier(CheckedBiFunction<T, T2, R> checkedBiFunction, T t, T2 t2) {
        return () -> checkedBiFunction.apply(t, t2);
    }

    /**
     * A  {@link CheckedSupplier} form of {@link CheckedTriFunction}
     *
     * @param checkedTriFunction input
     * @param t                 1st input to the checkedTriFunction
     * @param t2                2nd input to the checkedTriFunction
     * @param t3                3rd input to the checkedTriFunction
     * @param <T>               type of input
     * @param <T2>              type of input
     * @param <T3>              type of input
     * @param <R>               type of result
     * @return CheckedSupplier wrapped input
     */
    public static <T, T2, T3, R> CheckedSupplier<R> toCheckedSupplier(CheckedTriFunction<T, T2, T3, R> checkedTriFunction, T t, T2 t2, T3 t3) {
        return () -> checkedTriFunction.apply(t, t2, t3);
    }

    /**
     * A  {@link CheckedSupplier} form of {@link CheckedFunction}
     *
     * @param checkedFunction   input
     * @param t                 the input to the CheckedFunction
     * @param <T> type of input
     * @param <R> type of result
     * @return CheckedSupplier wrapped input
     */
    public static <T, R> CheckedSupplier<R> toCheckedSupplier(CheckedFunction<T, R> checkedFunction, T t) {
        return () -> checkedFunction.apply(t);
    }

    /**
     * An exception mapped supplier wrapper
     *
     * @param supplier to be wrapped
     * @param ex       a {@link Class<RuntimeException>} encountered by the callable
     * @param op       transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @param ex2      a {@link Class<RuntimeException>} encountered by the callable
     * @param op2      transforming {@link UnaryOperator} to transforming the ex2 to another exceotion
     * @param ex3      a {@link Class<RuntimeException>} encountered by the callable
     * @param op3      transforming {@link UnaryOperator} to transforming the ex3 to another exceotion
     * @param <X>      Ist type of exception to be mapped
     * @param <X2>     2nd type of exception to be mapped
     * @param <X3>     3rd type of exception to be mapped
     * @param <R>      type of the result
     * @return supplier wrapped
     */
    public static <X extends RuntimeException, X2 extends RuntimeException, X3 extends RuntimeException, R>
    Supplier<R> errorMappedSupplier(
            Supplier<R> supplier,
            Class<X> ex, UnaryOperator<Exception> op,
            Class<X2> ex2, UnaryOperator<Exception> op2,
            Class<X3> ex3, UnaryOperator<Exception> op3) {
        return () -> supplier.tryWrap().mapException(ex, op, ex2, op2, ex3, op3).getOrElseThrow(rteMapper);
    }

    /**
     * An exception mapped supplier wrapper
     *
     * @param supplier to be wrapped
     * @param ex       a {@link Class<RuntimeException>} encountered by the callable
     * @param op       transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @param ex2      a {@link Class<RuntimeException>} encountered by the callable
     * @param op2      transforming {@link UnaryOperator} to transforming the ex2 to another exceotion
     * @param <X>      Ist type of exception to be mapped
     * @param <X2>     2nd type of exception to be mapped
     * @param <R>      type of the result
     * @return supplier wrapped
     */
    public static <X extends RuntimeException, X2 extends RuntimeException, R>
    Supplier<R> errorMappedSupplier(
            Supplier<R> supplier,
            Class<X>  ex,  UnaryOperator<Exception> op,
            Class<X2> ex2, UnaryOperator<Exception> op2) {
        return () -> supplier.tryWrap().mapException(ex, op, ex2, op2).getOrElseThrow(rteMapper);
    }

    /**
     * An exception mapped supplier wrapper
     *
     * @param supplier to be wrapped
     * @param ex       a {@link Class<RuntimeException>} encountered by the callable
     * @param op       transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @param <X>      Ist type of exception to be mapped
     * @param <R>      type of the result
     * @return supplier wrapped
     */
    public static <X extends RuntimeException, R>
    Supplier<R> errorMappedSupplier(Supplier<R> supplier, Class<X> ex, UnaryOperator<Exception> op) {
        return () -> supplier.tryWrap().mapException(ex, op).getOrElseThrow(rteMapper);
    }

    /**
     * An exception consuming supplier wrapper
     *
     * @param supplier to be wrapped
     * @param ex       a {@link Class<RuntimeException>} encountered by the callable
     * @param op       a {@link Consumer} to consume the ex encountered
     * @param ex2      a {@link Class<RuntimeException>} encountered by the callable
     * @param op2      a {@link Consumer} to consume the ex encountered
     * @param ex3      a {@link Class<RuntimeException>} encountered by the callable
     * @param op3      a {@link Consumer} to consume the ex encountered
     * @param <X>      Ist type of exception to be consumed
     * @param <X2>     2nd type of exception to be consumed
     * @param <X3>     3rd type of exception to be consumed
     * @param <R>      type of the result
     * @return supplier wrapped
     */
    public static <X extends RuntimeException, X2 extends RuntimeException, X3 extends RuntimeException, R>
    Supplier<R> errorConsumedSupplier(Supplier<R> supplier, Class<X> ex, Consumer<X> op,
            Class<X2> ex2, Consumer<X2> op2, Class<X3> ex3, Consumer<X3> op3) {
        return () -> supplier.tryWrap().consumeFailure(ex, op, ex2, op2, ex3, op3).getOrElseThrow(rteMapper);
    }

    /**
     * An exception consuming supplier wrapper
     *
     * @param supplier to be wrapped
     * @param ex       a {@link Class<RuntimeException>} encountered by the callable
     * @param op       a {@link Consumer} to consume the ex encountered
     * @param ex2      a {@link Class<RuntimeException>} encountered by the callable
     * @param op2      a {@link Consumer} to consume the ex encountered
     * @param <X>      Ist type of exception to be consumed
     * @param <X2>     2nd type of exception to be consumed
     * @param <R>      type of the result
     * @return supplier wrapped
     */
    public static <X extends RuntimeException, X2 extends RuntimeException, R>
    Supplier<R> errorConsumedSupplier(Supplier<R> supplier, Class<X> ex, Consumer<X> op, Class<X2> ex2, Consumer<X2> op2) {
        return () -> supplier.tryWrap().consumeFailure(ex, op, ex2, op2).getOrElseThrow(rteMapper);
    }

    /**
     * An exception consuming supplier wrapper
     *
     * @param supplier to be wrapped
     * @param ex       a {@link Class<RuntimeException>} encountered by the callable
     * @param op       a {@link Consumer} to consume the ex encountered
     * @param <X>      Ist type of exception to be consumed
     * @param <R>      type of the result
     * @return supplier wrapped
     */
    public static <X extends RuntimeException, R>
    Supplier<R> errorConsumedSupplier(Supplier<R> supplier, Class<X> ex, Consumer<X> op) {
        return () -> supplier.tryWrap().consumeFailure(ex, op).getOrElseThrow(rteMapper);
    }

    /**
     * An exception consuming CheckedSupplier wrapper.
     *
     * @param supplier a {@link CheckedSupplier} to be wrapped
     * @param ex       a {@link Class<Exception>} encountered by the callable
     * @param op       a {@link Consumer} to consume the ex encountered
     * @param <X>      Ist type of exception to be consumed
     * @param <R>      type of the result
     * @return supplier wrapped
     */
    @SneakyThrows
    public static <X extends Exception, R>
    CheckedSupplier<R> errorConsumedCheckedSupplier(CheckedSupplier<R> supplier, Class<X> ex, Consumer<X> op) {
        return () -> supplier.tryWrap().consumeFailure(ex, op).getOrElseThrow(ceMapper);
    }

    /**
     * An exception consuming CheckedSupplier wrapper
     *
     * @param supplier to be wrapped
     * @param ex       a {@link Class<Exception>} encountered by the callable
     * @param op       a {@link Consumer} to consume the ex encountered
     * @param ex2      a {@link Class<Exception>} encountered by the callable
     * @param op2      a {@link Consumer} to consume the ex encountered
     * @param <X>      Ist type of exception to be consumed
     * @param <X2>     2nd type of exception to be consumed
     * @param <R>      type of the result
     * @return supplier wrapped
     */
    public static <X extends Exception, X2 extends Exception, R>
    CheckedSupplier<R> errorConsumedCheckedSupplier(CheckedSupplier<R> supplier,
            Class<X>  ex,  Consumer<X>  op, Class<X2> ex2, Consumer<X2> op2) {
        return () -> supplier.tryWrap().consumeFailure(ex, op, ex2, op2).getOrElseThrow(ceMapper);
    }

    /**
     * An exception consuming CheckedSupplier wrapper
     *
     * @param supplier to be wrapped
     * @param ex       a {@link Class<Exception>} encountered by the callable
     * @param op       a {@link Consumer} to consume the ex encountered
     * @param ex2      a {@link Class<Exception>} encountered by the callable
     * @param op2      a {@link Consumer} to consume the ex encountered
     * @param ex3      a {@link Class<Exception>} encountered by the callable
     * @param op3      a {@link Consumer} to consume the ex encountered
     * @param <X>      Ist type of exception to be consumed
     * @param <X2>     2nd type of exception to be consumed
     * @param <X3>     3rd type of exception to be consumed
     * @param <R>      type of the result
     * @return supplier wrapped
     */
    public static <X extends Exception, X2 extends Exception, X3 extends Exception, R>
    CheckedSupplier<R> errorConsumedCheckedSupplier(CheckedSupplier<R> supplier,  Class<X>  ex,  Consumer<X>  op,
                                                    Class<X2> ex2, Consumer<X2> op2, Class<X3> ex3, Consumer<X3> op3) {
        return () -> supplier.tryWrap().consumeFailure(ex, op, ex2, op2, ex3, op3).getOrElseThrow(ceMapper);
    }

    /**
     * An exception mapped supplier wrapper
     *
     * @param supplier to be wrapped
     * @param ex       a {@link Class<RuntimeException>} encountered by the callable
     * @param op       transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @param <X>      Ist type of exception to be mapped
     * @param <R>      type of the result
     * @return supplier wrapped
     */
    public static <X extends Exception, R>
    CheckedSupplier<R> errorMappedCheckedSupplier(CheckedSupplier<R> supplier, Class<X> ex, UnaryOperator<Exception> op) {
        return () -> supplier.tryWrap().mapException(ex, op).getOrElseThrow(ceMapper);
    }

    /**
     * An exception mapped CheckedSupplier wrapper
     *
     * @param supplier (@link CheckedSupplier} to be wrapped
     * @param ex       a {@link Class<Exception>} encountered by the callable
     * @param op       transforming {@link UnaryOperator} to transforming the ex to another exception
     * @param ex2      a {@link Class<Exception>} encountered by the callable
     * @param op2      transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @param <X>      Ist type of exception to be mapped
     * @param <X2>     2nd type of exception to be mapped
     * @param <R>      type of the result
     * @return supplier wrapped
     */
    public static <X extends Exception, X2 extends Exception, R>
    CheckedSupplier<R> errorMappedCheckedSupplier(CheckedSupplier<R> supplier,
            Class<X>  ex,  UnaryOperator<Exception> op,
            Class<X2> ex2, UnaryOperator<Exception> op2) {
        return () -> supplier.tryWrap().mapException(ex, op, ex2, op2).getOrElseThrow(ceMapper);
    }

    /**
     * An exception mapped CheckedSupplier wrapper
     *
     * @param supplier (@link CheckedSupplier} to be wrapped
     * @param ex       a {@code Class<Exception>} encountered by the callable
     * @param op       transforming {@link UnaryOperator} to transforming the ex to another exception
     * @param ex2      a {@code Class<Exception>} encountered by the callable
     * @param op2      transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @param ex3      a {@code Class<Exception>} encountered by the callable
     * @param op3      transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @param <X>      Ist type of exception to be mapped
     * @param <X2>     2nd type of exception to be mapped
     * @param <X3>     3rd type of exception to be mapped
     * @param <R>      type of the result
     * @return supplier wrapped
     */
    public static <X extends Exception, X2 extends Exception, X3 extends Exception, R>
    CheckedSupplier<R> errorMappedCheckedSupplier(
            CheckedSupplier<R> supplier,
            Class<X>  ex,  UnaryOperator<Exception> op,
            Class<X2> ex2, UnaryOperator<Exception> op2,
            Class<X3> ex3, UnaryOperator<Exception> op3) {
        return () -> supplier.tryWrap().mapException(ex, op, ex2, op2, ex3, op3).getOrElseThrow(ceMapper);
    }


}