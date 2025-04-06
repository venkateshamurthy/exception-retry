package io.github.venkateshamurthy.exceptional;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.functions.CheckedBiFunction;
import io.github.resilience4j.core.functions.CheckedConsumer;
import io.github.resilience4j.core.functions.CheckedFunction;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;

import java.util.function.*;

public class RxSupplier {

    public static <R> Supplier<R> retrySupplier(Supplier<R> supplier, Retry retry){ return Retry.decorateSupplier(retry, supplier);}
    public static <R> Supplier<R> rateLimitSupplier(Supplier<R> supplier, RateLimiter rateLimiter){ return RateLimiter.decorateSupplier(rateLimiter, supplier);}
    public static <R> Supplier<R> circuitBreakSupplier(Supplier<R> supplier, CircuitBreaker circuitBreaker){ return  CircuitBreaker.decorateSupplier(circuitBreaker, supplier);}
    public static <R> Supplier<R> bulkheadSupplier(Supplier<R> supplier, Bulkhead bulkhead){ return  Bulkhead.decorateSupplier(bulkhead, supplier); }

    public static <R> CheckedSupplier<R> retryCheckedSupplier(CheckedSupplier<R> supplier, Retry retry){ return Retry.decorateCheckedSupplier(retry, supplier);}
    public static <R> CheckedSupplier<R> rateLimitCheckedSupplier(CheckedSupplier<R> supplier, RateLimiter rateLimiter){ return RateLimiter.decorateCheckedSupplier(rateLimiter, supplier);}
    public static <R> CheckedSupplier<R> circuitBreakCheckedSupplier(CheckedSupplier<R> supplier, CircuitBreaker circuitBreaker){ return  CircuitBreaker.decorateCheckedSupplier(circuitBreaker, supplier);}
    public static <R> CheckedSupplier<R> bulkheadCheckedSupplier(CheckedSupplier<R> supplier, Bulkhead bulkhead){ return  Bulkhead.decorateCheckedSupplier(bulkhead, supplier); }

    // Convenience transforms from other forms to supplier
    public static <T>      Supplier<Void> rxSupplier(Consumer<T> bic, T t) {return () -> {bic.accept(t);return null;};}
    public static <T,T2>   Supplier<Void> rxSupplier(BiConsumer<T, T2> bic, T t, T2 t2) {return () -> {bic.accept(t, t2);return null;};}
    public static <T,T2,R> Supplier<R>    rxSupplier(BiFunction<T, T2, R> bic, T t, T2 t2) {return () -> bic.apply(t, t2);}
    public static <T,R>    Supplier<R>    rxSupplier(Function<T, R> f, T t) {return () -> f.apply(t); }

    public static <T>      CheckedSupplier<Void> rxCheckedSupplier(CheckedConsumer<T> bic, T t)   { return () ->  { bic.accept(t); return null;};}
    public static <T,T2>   CheckedSupplier<Void> rxCheckedSupplier(CheckedBiConsumer<T, T2> bic, T t, T2 t2)    { return () -> { bic.accept(t, t2); return null;};}
    public static <T,T2, R>CheckedSupplier<R>    rxCheckedSupplier(CheckedBiFunction<T, T2, R> bif, T t, T2 t2) { return () -> bif.apply(t, t2);}
    public static <T,R>    CheckedSupplier<R>    rxCheckedSupplier(CheckedFunction<T, R> f, T t)  { return () -> f.apply(t); }

}