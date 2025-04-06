package com.venkateshamurthy.exceptional;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static com.venkateshamurthy.exceptional.RxTry.ceMapper;
@Slf4j
@ExtensionMethod(RxTry.class)
public class RxCallable {
    public static <T> Callable<T> toCallable(Callable<T> callable) {return callable;}
    public static <T> Callable<T> retryCallable(Callable<T> callable, Retry retry) {
        return Retry.decorateCallable(retry, callable);
    }
    public static <T> Callable<T> rateLimitCallable(Callable<T> callable, RateLimiter rateLimiter) {
        return  RateLimiter.decorateCallable(rateLimiter, callable);
    }
    public static <T> Callable<T> circuitBreakCallable(Callable<T> callable, CircuitBreaker circuitBreaker){
        return  CircuitBreaker.decorateCallable(circuitBreaker, callable);
    }
    public static <T> Callable<T> bulkheadCallable(Callable<T> callable, Bulkhead bulkhead){
        return  Bulkhead.decorateCallable(bulkhead, callable);
    }

    public static <X extends Exception, T> Callable<T> errorMappedCallable(
            Callable<T> callable,
            Class<X> ex, UnaryOperator<Exception> op) {
        return ()->callable.tryWrap().mapException(ex, op).getOrElseThrow(ceMapper);
    }

    public static <X extends Exception, X2 extends Exception, T> Callable<T> errorMappedCallable(
            Callable<T> callable,
            Class<X> ex, UnaryOperator<Exception> op,
            Class<X2> ex2, UnaryOperator<Exception> op2) {
        return ()->callable.tryWrap().mapException(ex, op, ex2, op2).getOrElseThrow(ceMapper);
    }

    public static <X extends Exception, X2 extends Exception,X3 extends Exception,T> Callable<T> errorMappedCallable(
            Callable<T> callable,
            Class<X>  ex,   UnaryOperator<Exception> op,
            Class<X2> ex2, UnaryOperator<Exception> op2,
            Class<X3> ex3, UnaryOperator<Exception> op3) {
        return ()->callable.tryWrap().mapException(ex, op, ex2, op2, ex3, op3).getOrElseThrow(ceMapper);
    }

    public static <X extends Exception, T> Callable<T> errorMappedCallable(
            Callable<T> callable,
            Class<X> ex, Supplier<? extends Exception> op) {
        return ()->callable.tryWrap().mapException(ex, op).getOrElseThrow(ceMapper);
    }

    public static <X extends Exception, T> Callable<T> errorConsumedCallable(
            Callable<T> callable,
            Class<X> ex, Consumer<X> op) {
        return ()->callable.tryWrap().consumeFailure(ex, op).getOrElseThrow(ceMapper);
    }

    public static <X extends Exception, X2 extends Exception, T> Callable<T> errorConsumedCallable(
            Callable<T>  callable,
            Class<X>  ex,  Consumer<X>  op,
            Class<X2> ex2, Consumer<X2> op2) {
        return ()->callable.tryWrap().consumeFailure(ex, op, ex2, op2).getOrElseThrow(ceMapper);
    }

    public static <X extends Exception, X2 extends Exception, X3 extends Exception, T> Callable<T> errorConsumedCallable(
            Callable<T> callable,
            Class<X>  ex,  Consumer<X> op,
            Class<X2> ex2, Consumer<X2> op2,
            Class<X3> ex3, Consumer<X3> op3) {
        return ()->callable.tryWrap().consumeFailure(ex, op, ex2, op2, ex3, op3).getOrElseThrow(ceMapper);
    }

}