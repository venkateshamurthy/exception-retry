package com.venkateshamurthy.exceptional;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.functions.CheckedConsumer;
import io.github.resilience4j.core.functions.CheckedRunnable;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;

import java.util.function.BiConsumer;
import java.util.function.Consumer;


public class RxRunnable {
    public static  Runnable retryRunnable(Runnable runnable, Retry retry) {return Retry.decorateRunnable(retry, runnable);}
    public static  Runnable rateLimitRunnable(Runnable runnable, RateLimiter rateLimiter) {return  RateLimiter.decorateRunnable(rateLimiter, runnable);}
    public static  Runnable circuitBreakRunnable(Runnable runnable, CircuitBreaker circuitBreaker){return  CircuitBreaker.decorateRunnable(circuitBreaker, runnable);}
    public static  Runnable bulkheadRunnable(Runnable runnable, Bulkhead bulkhead){return  Bulkhead.decorateRunnable(bulkhead, runnable);}
    
    public static  CheckedRunnable retryCheckedRunnable(CheckedRunnable checkedRunnable, Retry retry) {return Retry.decorateCheckedRunnable(retry, checkedRunnable);}
    public static  CheckedRunnable rateLimitCheckedRunnable(CheckedRunnable checkedRunnable, RateLimiter rateLimiter) {return  RateLimiter.decorateCheckedRunnable(rateLimiter, checkedRunnable);}
    public static  CheckedRunnable circuitBreakCheckedRunnable(CheckedRunnable checkedRunnable, CircuitBreaker circuitBreaker){return  CircuitBreaker.decorateCheckedRunnable(circuitBreaker, checkedRunnable);}
    public static  CheckedRunnable bulkheadCheckedRunnable(CheckedRunnable checkedRunnable, Bulkhead bulkhead){return  Bulkhead.decorateCheckedRunnable(bulkhead, checkedRunnable);}

    public static <T>    Runnable rxRunnable(Consumer<T> bic, T t) {return () -> bic.accept(t);}
    public static <T,T2> Runnable rxRunnable(BiConsumer<T, T2> bic, T t, T2 t2) {return () -> bic.accept(t, t2);}
    public static <T>    CheckedRunnable rxCheckedRunnable(CheckedConsumer<T> bic, T t) {return () -> bic.accept(t);}
    public static <T,T2> CheckedRunnable rxCheckedRunnable(CheckedBiConsumer<T, T2> bic, T t, T2 t2) {return () -> bic.accept(t, t2);}
}