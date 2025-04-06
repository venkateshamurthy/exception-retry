package io.github.venkateshamurthy.exceptional;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.functions.CheckedBiFunction;
import io.github.resilience4j.core.functions.CheckedFunction;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;

import java.util.function.BiFunction;
import java.util.function.Function;

import static io.github.venkateshamurthy.exceptional.RxSupplier.rxCheckedSupplier;
import static io.github.venkateshamurthy.exceptional.RxSupplier.rxSupplier;


public class RxFunction  {
    public static <T, R> Function<T, R> retryFunction(Function<T, R> function, Retry retry) {return Retry.decorateFunction(retry, function);}
    public static <T, R> Function<T, R> rateLimitFunction(Function<T, R> function, RateLimiter rateLimiter) {return  RateLimiter.decorateFunction(rateLimiter, function);}
    public static <T, R> Function<T, R> circuitBreakFunction(Function<T, R> function, CircuitBreaker circuitBreaker){return  CircuitBreaker.decorateFunction(circuitBreaker, function);}
    public static <T, R> Function<T, R> bulkheadFunction(Function<T, R> function, Bulkhead bulkhead){return  Bulkhead.decorateFunction(bulkhead, function);}

    public static <T, R> CheckedFunction<T, R> retryCheckedFunction(CheckedFunction<T, R> checkedFunction, Retry retry) {return Retry.decorateCheckedFunction(retry, checkedFunction);}
    public static <T, R> CheckedFunction<T, R> rateLimitCheckedFunction(CheckedFunction<T, R> checkedFunction, RateLimiter rateLimiter) {return  RateLimiter.decorateCheckedFunction(rateLimiter, checkedFunction);}
    public static <T, R> CheckedFunction<T, R> circuitBreakCheckedFunction(CheckedFunction<T, R> checkedFunction, CircuitBreaker circuitBreaker){return  CircuitBreaker.decorateCheckedFunction(circuitBreaker, checkedFunction);}
    public static <T, R> CheckedFunction<T, R> bulkheadCheckedFunction(CheckedFunction<T, R> checkedFunction, Bulkhead bulkhead){return  Bulkhead.decorateCheckedFunction(bulkhead, checkedFunction);}

    public static <T, T2, R> BiFunction<T, T2, R> retryBiFunction(BiFunction<T, T2, R> function, Retry retry){return  (T t, T2 t2) -> Retry.decorateSupplier(retry, rxSupplier(function, t, t2)).get();}
    public static <T, T2, R> BiFunction<T, T2, R> rateLimitBiFunction(BiFunction<T, T2, R> function, RateLimiter rateLimiter){return  (T t, T2 t2) -> RateLimiter.decorateSupplier(rateLimiter, rxSupplier(function, t, t2)).get();}
    public static <T, T2, R> BiFunction<T, T2, R> circuitBreakBiFunction(BiFunction<T, T2, R> function, CircuitBreaker circuitBreaker){return  (T t, T2 t2) -> CircuitBreaker.decorateSupplier(circuitBreaker, rxSupplier(function, t, t2)).get();}
    public static <T, T2, R> BiFunction<T, T2, R> bulkheadBiFunction(BiFunction<T, T2, R> function, Bulkhead bulkhead){return  (T t, T2 t2) -> Bulkhead.decorateSupplier(bulkhead, rxSupplier(function, t, t2)).get();}

    public static <T, T2, R> CheckedBiFunction<T, T2, R> retryCheckedBiFunction(CheckedBiFunction<T, T2, R> checkedBiFunction, Retry retry){return  (T t, T2 t2) -> Retry.decorateCheckedSupplier(retry, rxCheckedSupplier(checkedBiFunction, t, t2)).get();}
    public static <T, T2, R> CheckedBiFunction<T, T2, R> rateLimitCheckedBiFunction(CheckedBiFunction<T, T2, R> checkedBiFunction, RateLimiter rateLimiter){return  (T t, T2 t2) -> RateLimiter.decorateCheckedSupplier(rateLimiter, rxCheckedSupplier(checkedBiFunction, t, t2)).get();}
    public static <T, T2, R> CheckedBiFunction<T, T2, R> circuitBreakCheckedBiFunction(CheckedBiFunction<T, T2, R> checkedBiFunction, CircuitBreaker circuitBreaker){return  (T t, T2 t2) -> CircuitBreaker.decorateCheckedSupplier(circuitBreaker, rxCheckedSupplier(checkedBiFunction, t, t2)).get();}
    public static <T, T2, R> CheckedBiFunction<T, T2, R> bulkheadCheckedBiFunction(CheckedBiFunction<T, T2, R> checkedBiFunction, Bulkhead bulkhead){return  (T t, T2 t2) -> Bulkhead.decorateCheckedSupplier(bulkhead, rxCheckedSupplier(checkedBiFunction, t, t2)).get();}
}