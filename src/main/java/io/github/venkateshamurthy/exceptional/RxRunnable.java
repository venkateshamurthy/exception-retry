package io.github.venkateshamurthy.exceptional;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.functions.CheckedConsumer;
import io.github.resilience4j.core.functions.CheckedRunnable;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import lombok.experimental.ExtensionMethod;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static io.github.venkateshamurthy.exceptional.RxTry.ceMapper;
import static io.github.venkateshamurthy.exceptional.RxTry.rteMapper;

/**
 * A Runnable wrapper convenience utility covering {@link Runnable}, {@link CheckedRunnable}
 */
@ExtensionMethod(RxTry.class)
public class RxRunnable {
    /** A reflexive wrapper to {@link Runnable} that helps in method chaining.*/
    public static Runnable toRunnable(Runnable runnable) {return runnable;}
    /** A reflexive wrapper to {@link CheckedRunnable} that helps in method chaining.*/
    public static CheckedRunnable toCheckedRunnable(CheckedRunnable runnable) {return runnable;}

    /**
     * A {@link Retry} wrapper on the {@link }Runnable}
     * @param runnable to be wrapped
     * @param retry to be decorating the runnable
     * @return Runnable
     */
    public static Runnable retryRunnable(
            final Runnable runnable, Retry retry) {
        return Retry.decorateRunnable(retry, runnable);
    }

    /**
     * A {@link RateLimiter} wrapper on the {@link }Runnable}
     * @param runnable to be wrapped
     * @param rateLimiter to be decorating the runnable
     * @return Runnable
     */
    public static Runnable rateLimitRunnable(
            final Runnable runnable, RateLimiter rateLimiter) {
        return RateLimiter.decorateRunnable(rateLimiter, runnable);
    }

    /**
     * A {@link CircuitBreaker} wrapper on the {@link }Runnable}
     * @param runnable to be wrapped
     * @param circuitBreaker to be decorating the runnable
     * @return Runnable
     */
    public static Runnable circuitBreakRunnable(
            final Runnable runnable, CircuitBreaker circuitBreaker) {
        return CircuitBreaker.decorateRunnable(circuitBreaker, runnable);
    }

    /**
     * A {@link Bulkhead} wrapper on the {@link }Runnable}
     * @param runnable to be wrapped
     * @param bulkhead to be decorating the runnable
     * @return Runnable
     */
    public static Runnable bulkheadRunnable(
            final Runnable runnable, Bulkhead bulkhead) {
        return Bulkhead.decorateRunnable(bulkhead, runnable);
    }

    /**
     * A {@link Runnable} transformer to the passed in {@link Consumer}
     * @param c the consumer
     * @param t input that consumer accepts
     * @return Runnable
     * @param <T> type of consumer input
     */
    public static <T> Runnable rxRunnable(
            final Consumer<T> c, T t) {
        return () -> c.accept(t);
    }

    /**
     * A {@link Runnable} transformer to the passed in {@link BiConsumer}
     * @param bic the consumer
     * @param t input that consumer accepts
     * @return Runnable
     * @param <T> type of consumer input
     * @param <T2> type of second parameter
     */
    public static <T, T2> Runnable rxRunnable(
            final BiConsumer<T, T2> bic, T t, T2 t2) {
        return () -> bic.accept(t, t2);
    }

    /**
     * A {@link Retry} wrappering {@link CheckedRunnable}
     * @param checkedRunnable to be wrapped
     * @param retry that is decorating the checkedRunnable
     * @return checkedRunnable
     */
    public static CheckedRunnable retryCheckedRunnable(
            final CheckedRunnable checkedRunnable, Retry retry) {
        return Retry.decorateCheckedRunnable(retry, checkedRunnable);
    }

    /**
     * A {@link RateLimiter} wrappering {@link CheckedRunnable}
     * @param checkedRunnable to be wrapped
     * @param rateLimiter that is decorating the checkedRunnable
     * @return checkedRunnable
     */
    public static CheckedRunnable rateLimitCheckedRunnable(
            final CheckedRunnable checkedRunnable, RateLimiter rateLimiter) {
        return RateLimiter.decorateCheckedRunnable(rateLimiter, checkedRunnable);
    }

    /**
     * A {@link CircuitBreaker} wrappering {@link CheckedRunnable}
     * @param checkedRunnable to be wrapped
     * @param circuitBreaker that is decorating the checkedRunnable
     * @return checkedRunnable
     */
    public static CheckedRunnable circuitBreakCheckedRunnable(
            final CheckedRunnable checkedRunnable, CircuitBreaker circuitBreaker) {
        return CircuitBreaker.decorateCheckedRunnable(circuitBreaker, checkedRunnable);
    }

    /**
     * A {@link Bulkhead} wrappering {@link CheckedRunnable}
     * @param checkedRunnable to be wrapped
     * @param bulkhead that is decorating the checkedRunnable
     * @return checkedRunnable
     */
    public static CheckedRunnable bulkheadCheckedRunnable(
            final CheckedRunnable checkedRunnable, Bulkhead bulkhead) {
        return Bulkhead.decorateCheckedRunnable(bulkhead, checkedRunnable);
    }

    /**
     * A {@link CheckedRunnable} wrapper on the {@link CheckedConsumer}
     * @param bic CheckedBiConsumer
     * @param t parameter 1
     * @return checked runnable
     * @param <T> type of 1st parameter
     */
    public static <T> CheckedRunnable rxCheckedRunnable(
            final CheckedConsumer<T> bic, T t) {
        return () -> bic.accept(t);
    }

    /**
     * A {@link CheckedRunnable} wrapper on the {@link CheckedBiConsumer}
     * @param bic CheckedBiConsumer
     * @param t parameter 1
     * @param t2 parameter 2
     * @return checked runnable
     * @param <T> type of 1st parameter
     * @param <T2> type of 2nd parameter
     */
    public static <T, T2> CheckedRunnable rxCheckedRunnable(
            final CheckedBiConsumer<T, T2> bic, T t, T2 t2) {
        return () -> bic.accept(t, t2);
    }

    /**
     * An exception mapped Runnable wrapper
     * @param runnable to be wrapped
     * @param ex a {@code Class<Exception>} encountered by the runnable
     * @param op transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @return runnable wrapped
     * @param <X> Ist type of exception to be mapped
     */
    public static <X extends RuntimeException>
    Runnable errorMappedRunnable(
            final Runnable runnable,
            Class<X> ex, UnaryOperator<Exception> op) {
        return ()->runnable.tryWrap().mapException(ex, op).getOrElseThrow(rteMapper);
    }

    /**
     * An exception mapped Runnable wrapper
     * @param runnable to be wrapped
     * @param ex a {@code Class<Exception>} encountered by the runnable
     * @param op transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @param ex2 a {@code Class<Exception>} encountered by the runnable
     * @param op2 tranaforming {@link UnaryOperator} to transforming the ex2 to another exceotion
     * @return runnable wrapped
     * @param <X> Ist type of exception to be mapped
     * @param <X2> 2nd type of exception to be mapped
     */
    public static <X extends RuntimeException, X2 extends RuntimeException>
    Runnable errorMappedRunnable(
            final Runnable runnable,
            Class<X>  ex,  UnaryOperator<Exception> op,
            Class<X2> ex2, UnaryOperator<Exception> op2) {
        return ()->runnable.tryWrap().mapException(ex, op, ex2, op2).getOrElseThrow(rteMapper);
    }

    /**
     * An exception mapped Runnable wrapper
     * @param runnable to be wrapped
     * @param ex a {@code Class<Exception>} encountered by the runnable
     * @param op transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @param ex2 a {@code Class<Exception>} encountered by the runnable
     * @param op2 tranaforming {@link UnaryOperator} to transforming the ex2 to another exceotion
     * @param ex3 a {@code Class<Exception>} encountered by the runnable
     * @param op3 tranaforming {@link UnaryOperator} to transforming the ex3 to another exceotion
     * @return runnable wrapped
     * @param <X> Ist type of exception to be mapped
     * @param <X2> 2nd type of exception to be mapped
     * @param <X3> 3rd type of exception to be mapped
     */
    public static <X extends RuntimeException, X2 extends RuntimeException, X3 extends RuntimeException>
    Runnable errorMappedRunnable(
            final Runnable runnable,
            Class<X>  ex,  UnaryOperator<Exception> op,
            Class<X2> ex2, UnaryOperator<Exception> op2,
            Class<X3> ex3, UnaryOperator<Exception> op3) {
        return ()->runnable.tryWrap().mapException(ex, op, ex2, op2, ex3, op3).getOrElseThrow(rteMapper);
    }

    /**
     * An exception consuming Runnable wrapper
     * @param runnable to be wrapped
     * @param ex a {@code Class<Exception>} encountered by the runnable
     * @param op a {@link Consumer} to consume the exception encountered
     * @return runnable wrapped
     * @param <X> Ist type of exception to be mapped
     */
    public static <X extends RuntimeException>
    Runnable errorConsumedRunnable(
            Runnable runnable,
            Class<X> ex, Consumer<X> op) {
        return ()->runnable.tryWrap().consumeFailure(ex, op).getOrElseThrow(rteMapper);
    }

    /**
     * An exception mapped Runnable wrapper
     * @param runnable to be wrapped
     * @param ex a {@code Class<Exception>} encountered by the runnable
     * @param op a {@link Consumer} to consume the exception encountered
     * @param ex2 a {@code Class<Exception>} encountered by the runnable
     * @param op2 a {@link Consumer} to consume the exception encountered
     * @return runnable wrapped
     * @param <X> Ist type of exception to be mapped
     * @param <X2> 2nd type of exception to be mapped
     */
    public static <X extends RuntimeException, X2 extends RuntimeException>
    Runnable errorConsumedRunnable(
            Runnable runnable,
            Class<X> ex, Consumer<X> op,
            Class<X2> ex2, Consumer<X2> op2) {
        return ()->runnable.tryWrap().consumeFailure(ex, op, ex2, op2).getOrElseThrow(rteMapper);
    }

    /**
     * An exception mapped Runnable wrapper
     * @param runnable to be wrapped
     * @param ex a {@code Class<Exception>} encountered by the runnable
     * @param op a {@link Consumer} to consume the exception encountered
     * @param ex2 a {@code Class<Exception>} encountered by the runnable
     * @param op2 a {@link Consumer} to consume the exception encountered
     * @param ex3 a {@code Class<Exception>} encountered by the runnable
     * @param op3 a {@link Consumer} to consume the exception encountered
     * @return runnable wrapped
     * @param <X> Ist type of exception to be mapped
     * @param <X2> 2nd type of exception to be mapped
     * @param <X3> 3rd type of exception to be mapped
     */
    public static <X extends RuntimeException, X2 extends RuntimeException, X3 extends RuntimeException>
    Runnable errorConsumedRunnable(
            Runnable runnable,
            Class<X> ex, Consumer<X> op,
            Class<X2> ex2, Consumer<X2> op2,
            Class<X3> ex3, Consumer<X3> op3) {
        return ()->runnable.tryWrap().consumeFailure(ex, op, ex2, op2, ex3, op3).getOrElseThrow(rteMapper);
    }

    // Check Runnable
    /**
     * An exception mapped CheckedRunnable wrapper
     * @param runnable to be wrapped
     * @param ex a {@code Class<Exception>} encountered by the runnable
     * @param op transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @return runnable wrapped
     * @param <X> Ist type of exception to be mapped
     */
    public static <X extends Exception>
    CheckedRunnable errorMappedCheckedRunnable(
            final CheckedRunnable runnable,
            Class<X> ex, UnaryOperator<Exception> op) {
        return ()->runnable.tryWrap().mapException(ex, op).getOrElseThrow(ceMapper);
    }

    /**
     * An exception mapped CheckedRunnable wrapper
     * @param runnable to be wrapped
     * @param ex a {@code Class<Exception>} encountered by the runnable
     * @param op transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @param ex2 a {@code Class<Exception>} encountered by the runnable
     * @param op2 tranaforming {@link UnaryOperator} to transforming the ex2 to another exceotion
     * @return runnable wrapped
     * @param <X> Ist type of exception to be mapped
     * @param <X2> 2nd type of exception to be mapped
     */
    public static <X extends Exception, X2 extends Exception>
    CheckedRunnable errorMappedCheckedRunnable(
            final CheckedRunnable runnable,
            Class<X> ex,   UnaryOperator<Exception> op,
            Class<X2> ex2, UnaryOperator<Exception> op2) {
        return ()->runnable.tryWrap().mapException(ex, op, ex2, op2).getOrElseThrow(ceMapper);
    }

    /**
     * An exception mapped CheckedRunnable wrapper
     * @param runnable to be wrapped
     * @param ex a {@code Class<Exception>} encountered by the runnable
     * @param op transforming {@link UnaryOperator} to transforming the ex to another exceotion
     * @param ex2 a {@code Class<Exception>} encountered by the runnable
     * @param op2 tranaforming {@link UnaryOperator} to transforming the ex2 to another exceotion
     * @param ex3 a {@code Class<Exception>} encountered by the runnable
     * @param op3 tranaforming {@link UnaryOperator} to transforming the ex3 to another exceotion
     * @return runnable wrapped
     * @param <X> Ist type of exception to be mapped
     * @param <X2> 2nd type of exception to be mapped
     * @param <X3> 3rd type of exception to be mapped
     */
    public static <X extends Exception, X2 extends Exception, X3 extends Exception>
    CheckedRunnable errorMappedCheckedRunnable(
            final CheckedRunnable runnable,
            Class<X> ex,   UnaryOperator<Exception> op,
            Class<X2> ex2, UnaryOperator<Exception> op2,
            Class<X3> ex3, UnaryOperator<Exception> op3) {
        return ()->runnable.tryWrap().mapException(ex, op, ex2, op2, ex3, op3).getOrElseThrow(ceMapper);
    }

    /**
     * An exception consuming checked runnable wrapper
     * @param runnable to be wrapped
     * @param ex  a {@code Class<Exception>} encountered by the runnable
     * @param op  a {@link Consumer} to consume the ex encountered
     * @return runnable wrapped
     * @param <X> Ist type of exception to be consumed
     */
    public static <X extends Exception>
    CheckedRunnable errorConsumedCheckedRunnable(
            CheckedRunnable runnable,
            Class<X> ex, Consumer<X> op) {
        return ()->runnable.tryWrap().consumeFailure(ex, op).getOrElseThrow(ceMapper);
    }

    /**
     * An exception consuming checked runnable wrapper
     * @param runnable to be wrapped
     * @param ex  a {@code Class<Exception>} encountered by the runnable
     * @param op  a {@link Consumer} to consume the ex encountered
     * @param ex2 a {@code Class<Exception>} encountered by the runnable
     * @param op2 a {@link Consumer} to consume the ex encountered
     * @return runnable wrapped
     * @param <X> Ist type of exception to be consumed
     * @param <X2> 2nd type of exception to be consumed
     */
    public static <X extends Exception, X2 extends Exception>
    CheckedRunnable errorConsumedCheckedRunnable(
            CheckedRunnable runnable,
            Class<X>  ex,  Consumer<X> op,
            Class<X2> ex2, Consumer<X2> op2) {
        return ()->runnable.tryWrap().consumeFailure(ex, op, ex2, op2).getOrElseThrow(ceMapper);
    }

    /**
     * An exception consuming checked runnable wrapper
     * @param runnable to be wrapped
     * @param ex  a {@code Class<Exception>} encountered by the runnable
     * @param op  a {@link Consumer} to consume the ex encountered
     * @param ex2 a {@code Class<Exception>} encountered by the runnable
     * @param op2 a {@link Consumer} to consume the ex encountered
     * @param ex3 a {@code Class<Exception>} encountered by the runnable
     * @param op3 a {@link Consumer} to consume the ex encountered
     * @return runnable wrapped
     * @param <X> Ist type of exception to be consumed
     * @param <X2> 2nd type of exception to be consumed
     * @param <X3> 3rd type of exception to be consumed
     */
    public static <X extends Exception, X2 extends Exception, X3 extends Exception>
    CheckedRunnable errorConsumedCheckedRunnable(
            CheckedRunnable runnable,
            Class<X>  ex,  Consumer<X> op,
            Class<X2> ex2, Consumer<X2> op2,
            Class<X3> ex3, Consumer<X3> op3) {
        return ()->runnable.tryWrap().consumeFailure(ex, op, ex2, op2, ex3, op3).getOrElseThrow(ceMapper);
    }
}