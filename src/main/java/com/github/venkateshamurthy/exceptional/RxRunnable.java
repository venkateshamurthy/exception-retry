package com.github.venkateshamurthy.exceptional;

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

import static com.github.venkateshamurthy.exceptional.RxTry.ceMapper;
import static com.github.venkateshamurthy.exceptional.RxTry.rteMapper;
import static org.apache.commons.lang3.exception.ExceptionUtils.throwableOfType;

@ExtensionMethod(RxTry.class)
public class RxRunnable {
    public static Runnable toRunnable(Runnable runnable) {return runnable;}
    public static CheckedRunnable toCheckedRunnable(CheckedRunnable runnable) {return runnable;}
    public static Runnable retryRunnable(
            final Runnable runnable, Retry retry) {
        return Retry.decorateRunnable(retry, runnable);
    }

    public static Runnable rateLimitRunnable(
            final Runnable runnable, RateLimiter rateLimiter) {
        return RateLimiter.decorateRunnable(rateLimiter, runnable);
    }

    public static Runnable circuitBreakRunnable(
            final Runnable runnable, CircuitBreaker circuitBreaker) {
        return CircuitBreaker.decorateRunnable(circuitBreaker, runnable);
    }

    public static Runnable bulkheadRunnable(
            final Runnable runnable, Bulkhead bulkhead) {
        return Bulkhead.decorateRunnable(bulkhead, runnable);
    }

    public static <T> Runnable rxRunnable(
            final Consumer<T> bic, T t) {
        return () -> bic.accept(t);
    }

    public static <T, T2> Runnable rxRunnable(
            final BiConsumer<T, T2> bic, T t, T2 t2) {
        return () -> bic.accept(t, t2);
    }

    public static CheckedRunnable retryCheckedRunnable(
            final CheckedRunnable checkedRunnable, Retry retry) {
        return Retry.decorateCheckedRunnable(retry, checkedRunnable);
    }

    public static CheckedRunnable rateLimitCheckedRunnable(
            final CheckedRunnable checkedRunnable, RateLimiter rateLimiter) {
        return RateLimiter.decorateCheckedRunnable(rateLimiter, checkedRunnable);
    }

    public static CheckedRunnable circuitBreakCheckedRunnable(
            final CheckedRunnable checkedRunnable, CircuitBreaker circuitBreaker) {
        return CircuitBreaker.decorateCheckedRunnable(circuitBreaker, checkedRunnable);
    }

    public static CheckedRunnable bulkheadCheckedRunnable(
            final CheckedRunnable checkedRunnable, Bulkhead bulkhead) {
        return Bulkhead.decorateCheckedRunnable(bulkhead, checkedRunnable);
    }

    public static <T> CheckedRunnable rxCheckedRunnable(
            final CheckedConsumer<T> bic, T t) {
        return () -> bic.accept(t);
    }

    public static <T, T2> CheckedRunnable rxCheckedRunnable(
            final CheckedBiConsumer<T, T2> bic, T t, T2 t2) {
        return () -> bic.accept(t, t2);
    }


    public static <X extends RuntimeException>
    Runnable errorMappedRunnable(
            final Runnable runnable,
            Class<X> ex, UnaryOperator<Exception> op) {
        return ()->runnable.tryWrap().mapException(ex, op).getOrElseThrow(rteMapper);
    }

    public static <X extends RuntimeException, X2 extends RuntimeException>
    Runnable errorMappedRunnable(
            final Runnable runnable,
            Class<X>  ex,  UnaryOperator<Exception> op,
            Class<X2> ex2, UnaryOperator<Exception> op2) {
        return ()->runnable.tryWrap().mapException(ex, op, ex2, op2).getOrElseThrow(rteMapper);
    }

    public static <X extends RuntimeException, X2 extends RuntimeException, X3 extends RuntimeException>
    Runnable errorMappedRunnable(
            final Runnable runnable,
            Class<X>  ex,  UnaryOperator<Exception> op,
            Class<X2> ex2, UnaryOperator<Exception> op2,
            Class<X3> ex3, UnaryOperator<Exception> op3) {
        return ()->runnable.tryWrap().mapException(ex, op, ex2, op2, ex3, op3).getOrElseThrow(rteMapper);
    }

    public static <X extends RuntimeException>
    Runnable errorConsumedRunnable(
            Runnable runnable,
            Class<X> ex, Consumer<X> op) {
        return ()->runnable.tryWrap().consumeFailure(ex, op).getOrElseThrow(rteMapper);
    }

    public static <X extends RuntimeException, X2 extends RuntimeException>
    Runnable errorConsumedRunnable(
            Runnable runnable,
            Class<X> ex, Consumer<X> op,
            Class<X2> ex2, Consumer<X2> op2) {
        return ()->runnable.tryWrap().consumeFailure(ex, op, ex2, op2).getOrElseThrow(rteMapper);
    }

    public static <X extends RuntimeException, X2 extends RuntimeException, X3 extends RuntimeException>
    Runnable errorConsumedRunnable(
            Runnable runnable,
            Class<X> ex, Consumer<X> op,
            Class<X2> ex2, Consumer<X2> op2,
            Class<X3> ex3, Consumer<X3> op3) {
        return ()->runnable.tryWrap().consumeFailure(ex, op, ex2, op2, ex3, op3).getOrElseThrow(rteMapper);
    }

    // Check Runnable
    public static <X extends Exception>
    CheckedRunnable errorMappedCheckedRunnable(
            final CheckedRunnable runnable,
            Class<X> ex, UnaryOperator<Exception> op) {
        return ()->runnable.tryWrap().mapException(ex, op).getOrElseThrow(ceMapper);
    }

    public static <X extends Exception, X2 extends Exception>
    CheckedRunnable errorMappedCheckedRunnable(
            final CheckedRunnable runnable,
            Class<X> ex,   UnaryOperator<Exception> op,
            Class<X2> ex2, UnaryOperator<Exception> op2) {
        return ()->runnable.tryWrap().mapException(ex, op, ex2, op2).getOrElseThrow(ceMapper);
    }

    public static <X extends Exception, X2 extends Exception, X3 extends Exception>
    CheckedRunnable errorMappedCheckedRunnable(
            final CheckedRunnable runnable,
            Class<X> ex,   UnaryOperator<Exception> op,
            Class<X2> ex2, UnaryOperator<Exception> op2,
            Class<X3> ex3, UnaryOperator<Exception> op3) {
        return ()->runnable.tryWrap().mapException(ex, op, ex2, op2, ex3, op3).getOrElseThrow(ceMapper);
    }

    public static <X extends Exception>
    CheckedRunnable errorConsumedCheckedRunnable(
            CheckedRunnable runnable,
            Class<X> ex, Consumer<X> op) {
        return ()->runnable.tryWrap().consumeFailure(ex, op).getOrElseThrow(ceMapper);
    }

    public static <X extends Exception, X2 extends Exception>
    CheckedRunnable errorConsumedCheckedRunnable(
            CheckedRunnable runnable,
            Class<X>  ex,  Consumer<X> op,
            Class<X2> ex2, Consumer<X2> op2) {
        return ()->runnable.tryWrap().consumeFailure(ex, op, ex2, op2).getOrElseThrow(ceMapper);
    }

    public static <X extends Exception, X2 extends Exception, X3 extends Exception>
    CheckedRunnable errorConsumedCheckedRunnable(
            CheckedRunnable runnable,
            Class<X>  ex,  Consumer<X> op,
            Class<X2> ex2, Consumer<X2> op2,
            Class<X3> ex3, Consumer<X3> op3) {
        return ()->runnable.tryWrap().consumeFailure(ex, op, ex2, op2, ex3, op3).getOrElseThrow(ceMapper);
    }
}