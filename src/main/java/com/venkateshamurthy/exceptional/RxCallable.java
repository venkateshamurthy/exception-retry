package com.venkateshamurthy.exceptional;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;

import java.util.concurrent.Callable;

public class RxCallable {
    public static <T> Callable<T> retryCallable(Callable<T> callable, Retry retry) {return Retry.decorateCallable(retry, callable);}
    public static <T> Callable<T> rateLimitCallable(Callable<T> callable, RateLimiter rateLimiter) {return  RateLimiter.decorateCallable(rateLimiter, callable);}
    public static <T> Callable<T> circuitBreakCallable(Callable<T> callable, CircuitBreaker circuitBreaker){return  CircuitBreaker.decorateCallable(circuitBreaker, callable);}
    public static <T> Callable<T> bulkheadCallable(Callable<T> callable, Bulkhead bulkhead){return  Bulkhead.decorateCallable(bulkhead, callable);}
}