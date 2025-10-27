package io.github.venkateshamurthy.exceptional;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static io.github.venkateshamurthy.exceptional.RxTry.ceMapper;

/**
 * Callable wrapper functions covering resilience and Try.
 */
@Slf4j
@ExtensionMethod(RxTry.class)
@UtilityClass  @SuppressWarnings("java:S1118")
public class RxCallable {
    /**
     * A reflexive wrapper on the callable that helps in method chaining
     *
     * @param callable passed {@link Callable}
     * @param <T>      type of the result
     * @return the same callable passed in.
     */
    public static <T> Callable<T> toCallable(Callable<T> callable) {
        return callable;
    }

    /**
     * .
     * A Retry wrapper
     *
     * @param callable that is wrapped with retried
     * @param retry    a {@link Retry} to re attempt failing executions of this callable
     * @param <T>      type of the result
     * @return callable wrapped
     */
    public static <T> Callable<T> retryCallable(Callable<T> callable, Retry retry) {
        return Retry.decorateCallable(retry, callable);
    }

    /**
     * A RateLimiting wrapper
     *
     * @param callable    that is wrapped with ratelimited
     * @param rateLimiter a {@link RateLimiter} to re attempt limiting the calls for failing executions of this callable
     * @param <T>         type of the result
     * @return callable wrapped
     */
    public static <T> Callable<T> rateLimitCallable(Callable<T> callable, RateLimiter rateLimiter) {
        return RateLimiter.decorateCallable(rateLimiter, callable);
    }

    /**
     * A CircuitBreaker wrapper
     *
     * @param callable       that is wrapped with circuit broken
     * @param circuitBreaker a {@link CircuitBreaker} to break the circuit and navigate through half-open state to open etc
     * @param <T>            type of the result
     * @return callable wrapped
     */
    public static <T> Callable<T> circuitBreakCallable(Callable<T> callable, CircuitBreaker circuitBreaker) {
        return CircuitBreaker.decorateCallable(circuitBreaker, callable);
    }

    /**
     * A Bulkhead wrapper
     *
     * @param callable that is wrapped with bulk-headed
     * @param bulkhead a {@link Bulkhead} to wrap the callable
     * @param <T>      type of the result
     * @return callable wrapped
     */
    public static <T> Callable<T> bulkheadCallable(Callable<T> callable, Bulkhead bulkhead) {
        return Bulkhead.decorateCallable(bulkhead, callable);
    }

    /**
     * An exception mapped callable wrapper
     *
     * @param callable to be wrapped
     * @param ex       a {@code Class<Exception>} encountered by the callable
     * @param op       transforming {@link UnaryOperator} to transform the ex3 to another exception
     * @param <X>      {@link Exception} type
     * @param <T>      result type
     * @return callable wrapped
     */
    public static <X extends Exception, T> Callable<T> errorMappedCallable(
            Callable<T> callable,
            Class<X> ex, UnaryOperator<Exception> op) {
        return () -> callable.tryWrap().mapException(ex, op).getOrElseThrow(ceMapper);
    }

    /**
     * An exception mapped callable wrapper
     *
     * @param callable to be wrapped
     * @param ex       a {@code Class<Exception>} encountered by the callable
     * @param op       transforming {@link UnaryOperator} to transform the ex to another exception
     * @param ex2      a {@code Class<Exception>} encountered by the callable
     * @param op2      tranaforming {@link UnaryOperator} to transform the ex2 to another exception
     * @param <X>      Ist type of exception to be mapped
     * @param <X2>     2nd type of exception to be mapped
     * @param <T>      type of the result
     * @return callable wrapped
     */
    public static <X extends Exception, X2 extends Exception, T> Callable<T> errorMappedCallable(
            Callable<T> callable,
            Class<X> ex, UnaryOperator<Exception> op,
            Class<X2> ex2, UnaryOperator<Exception> op2) {
        return () -> callable.tryWrap().mapException(ex, op, ex2, op2).getOrElseThrow(ceMapper);
    }

    /**
     * An exception mapped callable wrapper
     *
     * @param callable to be wrapped
     * @param ex       a {@code Class<Exception>} encountered by the callable
     * @param op       transforming {@link UnaryOperator} to transform the ex to another exception
     * @param ex2      a {@code Class<Exception>} encountered by the callable
     * @param op2      tranaforming {@link UnaryOperator} to transform the ex2 to another exception
     * @param ex3      a {@code Class<Exception>} encountered by the callable
     * @param op3      tranaforming {@link UnaryOperator} to transform the ex3 to another exception
     * @param <X>      Ist type of exception to be mapped
     * @param <X2>     2nd type of exception to be mapped
     * @param <X3>     3rd type of exception to be mapped
     * @param <T>      type of the result
     * @return callable wrapped
     */
    public static <X extends Exception, X2 extends Exception, X3 extends Exception, T> Callable<T> errorMappedCallable(
            Callable<T> callable,
            Class<X> ex, UnaryOperator<Exception> op,
            Class<X2> ex2, UnaryOperator<Exception> op2,
            Class<X3> ex3, UnaryOperator<Exception> op3) {
        return () -> callable.tryWrap().mapException(ex, op, ex2, op2, ex3, op3).getOrElseThrow(ceMapper);
    }

    /**
     * An exception mapped callable wrapper
     *
     * @param callable to be wrapped
     * @param ex       a {@code Class<Exception>} encountered by the callable
     * @param op       a {@link Supplier} to provide an alternate exception
     * @param <X>      type of exception
     * @param <T>      type of result
     * @return callable wrapped
     */
    public static <X extends Exception, T> Callable<T> errorMappedCallable(
            Callable<T> callable, Class<X> ex, Supplier<Exception> op) {
        return () -> callable.tryWrap().mapException(ex, op).getOrElseThrow(ceMapper);
    }

    /**
     * An exception mapped callable wrapper
     *
     * @param callable to be wrapped
     * @param ex       a {@code Class<Exception>} encountered by the callable
     * @param op       transforming {@link Supplier} to provide an alternate exception
     * @param ex2      a {@code Class<Exception>} encountered by the callable
     * @param op2      tranaforming {@link Supplier} to provide an alternate exception
     * @param <X>      Ist type of exception to be mapped
     * @param <X2>     2nd type of exception to be mapped
     * @param <T>      type of the result
     * @return callable wrapped
     */
    public static <X extends Exception, X2 extends Exception, T> Callable<T> errorMappedCallable(
            Callable<T> callable,
            Class<X> ex, Supplier<Exception> op,
            Class<X2> ex2, Supplier<Exception> op2) {
        return () -> callable.tryWrap().mapException(ex, op, ex2, op2).getOrElseThrow(ceMapper);
    }

    /**
     * An exception mapped callable wrapper
     *
     * @param callable to be wrapped
     * @param ex       a {@code Class<Exception>} encountered by the callable
     * @param op       transforming {@link Supplier} to provide an alternate exception
     * @param ex2      a {@code Class<Exception>} encountered by the callable
     * @param op2      tranaforming {@link Supplier} to provide an alternate exception
     * @param ex3      a {@code Class<Exception>} encountered by the callable
     * @param op3      tranaforming {@link Supplier} to provide an alternate exception
     * @param <X>      Ist type of exception to be mapped
     * @param <X2>     2nd type of exception to be mapped
     * @param <X3>     3rd type of exception to be mapped
     * @param <T>      type of the result
     * @return callable wrapped
     */
    public static <X extends Exception, X2 extends Exception, X3 extends Exception, T> Callable<T> errorMappedCallable(
            Callable<T> callable,
            Class<X> ex, Supplier<Exception> op,
            Class<X2> ex2, Supplier<Exception> op2,
            Class<X3> ex3, Supplier<Exception> op3) {
        return () -> callable.tryWrap().mapException(ex, op, ex2, op2, ex3, op3).getOrElseThrow(ceMapper);
    }


    /**
     * An exception consuming callable wrapper
     *
     * @param callable to be wrapped
     * @param ex       a {@code Class<Exception>} encountered by the callable
     * @param op       a {@link Consumer} to consume the ex encountered
     * @param <X>      Ist type of exception to be consumed
     * @param <T>      type of the result
     * @return callable wrapped
     */
    public static <X extends Exception, T> Callable<T> errorConsumedCallable(
            Callable<T> callable,
            Class<X> ex, Consumer<X> op) {
        return () -> callable.tryWrap().consumeFailure(ex, op).getOrElseThrow(ceMapper);
    }

    /**
     * An exception consuming callable wrapper
     *
     * @param callable to be wrapped
     * @param ex       a {@code Class<Exception>} encountered by the callable
     * @param op       a {@link Consumer} to consume the ex encountered
     * @param ex2      a {@code Class<Exception>} encountered by the callable
     * @param op2      a {@link Consumer} to consume the ex encountered
     * @param <X>      Ist type of exception to be consumed
     * @param <X2>     2nd type of exception to be consumed
     * @param <T>      type of the result
     * @return callable wrapped
     */
    public static <X extends Exception, X2 extends Exception, T> Callable<T> errorConsumedCallable(
            Callable<T> callable,
            Class<X> ex, Consumer<X> op,
            Class<X2> ex2, Consumer<X2> op2) {
        return () -> callable.tryWrap().consumeFailure(ex, op, ex2, op2).getOrElseThrow(ceMapper);
    }

    /**
     * An exception consuming callable wrapper
     *
     * @param callable to be wrapped
     * @param ex       a {@code Class<Exception>} encountered by the callable
     * @param op       a {@link Consumer} to consume the ex encountered
     * @param ex2      a {@code Class<Exception>} encountered by the callable
     * @param op2      a {@link Consumer} to consume the ex encountered
     * @param ex3      a {@code Class<Exception>} encountered by the callable
     * @param op3      a {@link Consumer} to consume the ex encountered
     * @param <X>      Ist type of exception to be consumed
     * @param <X2>     2nd type of exception to be consumed
     * @param <X3>     3rd type of exception to be consumed
     * @param <T>      type of the result
     * @return callable wrapped
     */
    public static <X extends Exception, X2 extends Exception, X3 extends Exception, T> Callable<T> errorConsumedCallable(
            Callable<T> callable,
            Class<X> ex, Consumer<X> op,
            Class<X2> ex2, Consumer<X2> op2,
            Class<X3> ex3, Consumer<X3> op3) {
        return () -> callable.tryWrap().consumeFailure(ex, op, ex2, op2, ex3, op3).getOrElseThrow(ceMapper);
    }
}